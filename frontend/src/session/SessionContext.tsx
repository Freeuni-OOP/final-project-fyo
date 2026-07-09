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
import { auth } from "../firebase";
import { AuthApiError, authRequest, type AuthUser } from "../authApi";

const CURRENT_USER_ID_KEY = "fyo.currentUserId";

export type SessionStatus = "loading" | "authed" | "anon";

export interface SignUpFields {
  email: string;
  password: string;
  name: string;
  surname: string;
}

export interface Session {
  /** `loading` spans the Firebase handshake. Render a splash, never the landing page. */
  status: SessionStatus;
  user: AuthUser | null;
  error: string | null;
  signIn(email: string, password: string): Promise<void>;
  signUp(fields: SignUpFields): Promise<void>;
  signOut(): Promise<void>;
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

/**
 * Exchanges a Firebase identity for the backend account, deduplicating by uid:
 * `signIn` and the auth-state listener both ask for the same user microtasks
 * apart, and StrictMode runs effects twice in dev.
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
      try {
        return await authRequest("/api/auth/login", idToken);
      } catch (err) {
        // A verified Firebase account with no backend row: signup died between
        // creating the Firebase user and reaching our API. Recover by creating
        // the row now — nameless, because we no longer have those fields.
        if (err instanceof AuthApiError && err.status === 404) {
          return await authRequest("/api/auth/signup", idToken);
        }
        throw err;
      }
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

  /**
   * Signup posts the backend row itself so it can attach name and surname. The
   * auth-state listener must stand down while it runs: creating the Firebase
   * user fires the listener, whose 404 recovery posts a *nameless* signup, and
   * `AuthService.signup` is idempotent by uid — first request wins, so the
   * named one would silently lose the name.
   */
  const signingUp = useRef(false);

  const adopt = useCallback((authUser: AuthUser) => {
    storeUserId(authUser.id);
    setUser(authUser);
    setError(null);
    setStatus("authed");
  }, []);

  useEffect(() => {
    let alive = true;

    const unsubscribe = onAuthStateChanged(auth, async (firebaseUser) => {
      if (signingUp.current) return;

      if (!firebaseUser) {
        clearUserId();
        if (!alive) return;
        setUser(null);
        setStatus("anon");
        return;
      }

      try {
        const authUser = await resolveBackendUser(firebaseUser, pending.current);
        if (!alive) return;
        adopt(authUser);
      } catch (err) {
        if (!alive) return;
        setUser(null);
        setError(messageOf(err));
        setStatus("anon");
      }
    });

    return () => {
      alive = false;
      unsubscribe();
    };
  }, [adopt]);

  const signIn = useCallback(
    async (email: string, password: string) => {
      const credential = await signInWithEmailAndPassword(auth, email, password);
      adopt(await resolveBackendUser(credential.user, pending.current));
    },
    [adopt]
  );

  const signUp = useCallback(
    async ({ email, password, name, surname }: SignUpFields) => {
      signingUp.current = true;
      try {
        let credential: UserCredential;
        try {
          credential = await createUserWithEmailAndPassword(auth, email, password);
        } catch (err) {
          if (err instanceof FirebaseError && err.code === "auth/email-already-in-use") {
            credential = await signInWithEmailAndPassword(auth, email, password);
          } else {
            throw err;
          }
        }

        const idToken = await credential.user.getIdToken();
        adopt(await authRequest("/api/auth/signup", idToken, { name, surname }));
      } finally {
        signingUp.current = false;
      }
    },
    [adopt]
  );

  const refresh = useCallback(async () => {
    const firebaseUser = auth.currentUser;
    if (!firebaseUser) {
      setUser(null);
      setStatus("anon");
      return;
    }
    const idToken = await firebaseUser.getIdToken();
    adopt(await authRequest("/api/auth/login", idToken));
  }, [adopt]);

  const signOut = useCallback(async () => {
    // Leave the guarded route *before* dropping the account, and leave to
    // `#/home` rather than `#/`: both `#/app` + anon and `#/` + authed are
    // redirects, so either order out of those two would bounce through one.
    // `#/home` renders the landing page whatever the status is.
    window.location.hash = "#/home";
    await firebaseSignOut(auth);
    clearUserId();
    setUser(null);
    setError(null);
    setStatus("anon");
  }, []);

  const value = useMemo<Session>(
    () => ({ status, user, error, signIn, signUp, signOut, refresh }),
    [status, user, error, signIn, signUp, signOut, refresh]
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
