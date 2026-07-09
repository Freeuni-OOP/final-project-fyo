import { useEffect, useState, useCallback } from "react";
import { onAuthStateChanged, signOut as firebaseSignOut, type User } from "firebase/auth";
import { auth } from "../firebase";

export interface AuthSession {
  user: User | null;
  loading: boolean;
  getIdToken: () => Promise<string | null>;
  signOut: () => Promise<void>;
}

/**
 * Tracks the Firebase auth session so profile (and later other) pages can
 * grab a fresh ID token for Bearer-authenticated API calls.
 */
export function useAuth(): AuthSession {
  const [user, setUser] = useState<User | null>(auth.currentUser);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsub = onAuthStateChanged(auth, (next) => {
      setUser(next);
      setLoading(false);
    });
    return unsub;
  }, []);

  const getIdToken = useCallback(async (): Promise<string | null> => {
    const current = auth.currentUser;
    if (!current) return null;
    return current.getIdToken();
  }, []);

  const signOut = useCallback(async () => {
    await firebaseSignOut(auth);
  }, []);

  return { user, loading, getIdToken, signOut };
}
