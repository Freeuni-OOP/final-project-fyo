import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from "react";
import { FirebaseError } from "firebase/app";
import {
  createUserWithEmailAndPassword,
  onAuthStateChanged,
  signInWithEmailAndPassword,
  signOut as firebaseSignOut,
  type User as FirebaseUser,
  type UserCredential,
} from "firebase/auth";
import { auth, requireFirebaseAuth } from "../firebase";
import { AuthApiError, authRequest, type AuthUser } from "../authApi";

const CURRENT_USER_ID_KEY = "fyo.currentUserId";

/** `error` means a Firebase identity exists but the backend account could not
 *  be loaded — recoverable, so we offer a retry instead of dropping the user. */
export type SessionStatus = "loading" | "authed" | "anon" | "error";

export interface SignUpFields {
  email: string;
  password: string;
  name: string;
  surname: string;
}

export interface Session {
  status: SessionStatus;
  user: AuthUser | null;
  error: string | null;
  signIn(email: string, password: string): Promise<void>;
  signUp(fields: SignUpFields): Promise<void>;
  signOut(): Promise<void>;
  /** Retries the backend lookup after a transient failure. */
  retry(): Promise<void>;
  /** Re-reads the account. Onboarding needs this: it flips `user.onboarding`. */
  refresh(): Promise<void>;
}

const SessionContext = createContext<Session | null>(null);

/**
 * Written to both stores because `readCurrentUserId()` in teams/ and api/ reads
 * sessionStorage first and falls back to localStorage. localStorage alone would
 * leave a stale sessionStorage id from an earlier account shadowing this one.
 */
function storeUserId(id: number): void {
  try {
    sessionStorage.setItem(CURRENT_USER_ID_KEY, String(id));
    localStorage.setItem(CURRENT_USER_ID_KEY, String(id));
  } catch {
    /* private mode etc. */
  }
}

function clearUserId(): void {
  try {
    sessionStorage.removeItem(CURRENT_USER_ID_KEY);
    localStorage.removeItem(CURRENT_USER_ID_KEY);
  } catch {
    /* private mode etc. */
  }
}

function messageOf(err: unknown): string {
  return err instanceof Error ? err.message : "Could not load your account.";
}

/** Drops the Firebase session, ignoring failures — we are already on an error path. */
async function abandonFirebaseSession(): Promise<void> {
  if (!auth) return;
  try {
    if (auth) await firebaseSignOut(auth);
  } catch {
    /* nothing better to do */
  }
}

/**
 * Loads the backend account for a Firebase identity, deduplicating by uid
 * because StrictMode runs effects twice in dev.
 *
 * Only ever calls `login`. It must never fall back to `signup`: that endpoint
 * is idempotent by uid and takes name/surname from the body, so a nameless
 * recovery call here would permanently create — or worse, silently win the
 * race against — a real signup and leave the account with an empty name.
 */
function resolveBackendUser(
  firebaseUser: FirebaseUser,
  pending: Map<string, Promise<AuthUser>>
): Promise<AuthUser> {
  const inFlight = pending.get(firebaseUser.uid);
  if (inFlight) return inFlight;

  const lookup = (async () => {
    try {
      const idToken = await firebaseUser.getIdToken();
      return await authRequest("/api/auth/login", idToken);
    } finally {
      pending.delete(firebaseUser.uid);
    }
  })();

  pending.set(firebaseUser.uid, lookup);
  return lookup;
}

export function SessionProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<SessionStatus>("loading");
  const [user, setUser] = useState<AuthUser | null>(null);
  const [error, setError] = useState<string | null>(null);

  const pending = useRef(new Map<string, Promise<AuthUser>>());

  /** True while signIn/signUp own the state transitions, so the auth-state
   *  listener stays out of their way. Signup especially: creating the Firebase
   *  user fires the listener, and both would race to define the account. */
  const inFlow = useRef(false);

  const adopt = useCallback((authUser: AuthUser) => {
    storeUserId(authUser.id);
    setUser(authUser);
    setError(null);
    setStatus("authed");
  }, []);

  /** Clears local state without touching Firebase or the stored error message. */
  const forgetUser = useCallback(() => {
    clearUserId();
    setUser(null);
    setStatus("anon");
  }, []);

  useEffect(() => {
    let alive = true;

    if (!auth) {
      clearUserId();
      setUser(null);
      setStatus("anon");
      return;
    }

    const unsubscribe = onAuthStateChanged(auth, async (firebaseUser) => {
      if (inFlow.current) return;

      if (!firebaseUser) {
        clearUserId();
        if (!alive) return;
        setUser(null);
        setStatus("anon");
        // `error` is left alone: a sign-out forced by the branches below has
        // already set the message this listener would otherwise wipe.
        return;
      }

      try {
        const authUser = await resolveBackendUser(firebaseUser, pending.current);
        if (!alive) return;
        adopt(authUser);
      } catch (err) {
        // 404: a Firebase identity with no backend row, from a signup that died
        // after the Firebase account was created. We cannot repair it here —
        // we have no name or surname — so drop the orphan and ask them to sign
        // up again, which handles `email-already-in-use` and fills the row in.
        if (err instanceof AuthApiError && err.status === 404) {
          if (alive) setError("No account for this sign-in yet. Please sign up.");
          await abandonFirebaseSession();
          if (!alive) return;
          forgetUser();
          return;
        }

        // Anything else (backend down, 5xx) is transient. Keep the Firebase
        // session and surface a retry rather than silently signing them out.
        if (!alive) return;
        setUser(null);
        setError(messageOf(err));
        setStatus("error");
      }
    });

    return () => {
      alive = false;
      unsubscribe();
    };
  }, [adopt, forgetUser]);

  const signIn = useCallback(
    async (email: string, password: string) => {
      inFlow.current = true;
      try {
        const credential = await signInWithEmailAndPassword(requireFirebaseAuth(), email, password);
        try {
          adopt(await resolveBackendUser(credential.user, pending.current));
        } catch (err) {
          // Never leave a Firebase session we could not back with an account.
          await abandonFirebaseSession();
          forgetUser();
          throw err;
        }
      } finally {
        inFlow.current = false;
      }
    },
    [adopt, forgetUser]
  );

  const signUp = useCallback(
    async ({ email, password, name, surname }: SignUpFields) => {
      inFlow.current = true;
      try {
        let credential: UserCredential;
        try {
          credential = await createUserWithEmailAndPassword(requireFirebaseAuth(), email, password);
        } catch (err) {
          // The Firebase account can outlive a failed signup. If the password
          // matches, sign in and let the idempotent backend signup below fill
          // in the missing row — this time with the name.
          if (err instanceof FirebaseError && err.code === "auth/email-already-in-use") {
            credential = await signInWithEmailAndPassword(requireFirebaseAuth(), email, password);
          } else {
            throw err;
          }
        }

        try {
          const idToken = await credential.user.getIdToken();
          adopt(await authRequest("/api/auth/signup", idToken, { name, surname }));
        } catch (err) {
          // The backend rejected us after Firebase accepted. Roll the Firebase
          // side back so a reload can't resurrect a half-made, nameless account.
          await abandonFirebaseSession();
          forgetUser();
          throw err;
        }
      } finally {
        inFlow.current = false;
      }
    },
    [adopt, forgetUser]
  );

  const retry = useCallback(async () => {
    const firebaseUser = auth?.currentUser;
    if (!firebaseUser) {
      forgetUser();
      return;
    }

    setStatus("loading");
    try {
      adopt(await resolveBackendUser(firebaseUser, pending.current));
    } catch (err) {
      if (err instanceof AuthApiError && err.status === 404) {
        setError("No account for this sign-in yet. Please sign up.");
        await abandonFirebaseSession();
        forgetUser();
        return;
      }
      setError(messageOf(err));
      setStatus("error");
    }
  }, [adopt, forgetUser]);

  const refresh = useCallback(async () => {
    const firebaseUser = auth?.currentUser;
    if (!firebaseUser) {
      forgetUser();
      return;
    }
    const idToken = await firebaseUser.getIdToken();
    adopt(await authRequest("/api/auth/login", idToken));
  }, [adopt, forgetUser]);

  const signOut = useCallback(async () => {
    // Leave the guarded route *before* dropping the account, and leave to
    // `#/home` rather than `#/`: both `#/app` + anon and `#/` + authed are
    // redirects, so either order out of those two would bounce through one.
    // `#/home` renders the landing page whatever the status is.
    window.location.hash = "#/home";
    if (auth) await firebaseSignOut(auth);
    clearUserId();
    setUser(null);
    setError(null);
    setStatus("anon");
  }, []);

  const value = useMemo<Session>(
    () => ({ status, user, error, signIn, signUp, signOut, retry, refresh }),
    [status, user, error, signIn, signUp, signOut, retry, refresh]
  );

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}

export function useSession(): Session {
  const session = useContext(SessionContext);
  if (!session) throw new Error("useSession must be used inside <SessionProvider>");
  return session;
}

/** The name to greet someone with, falling back through the fields we may not have. */
export function displayNameOf(user: AuthUser): string {
  return user.name?.trim() || user.username || "player";
}


