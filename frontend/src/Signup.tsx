import { useState } from "react";
import { FirebaseError } from "firebase/app";
import {
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  type UserCredential,
} from "firebase/auth";
import { auth } from "./firebase";
import { authRequest } from "./authApi";
import { Button, Wordmark } from "./teams/ui";
import "./teams/theme.css";
import "./teams/teams.css";
import "./auth.css";

const goHome = () => {
  window.location.hash = "#/";
};

export default function Signup() {
  const [name, setName] = useState("");
  const [surname, setSurname] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [confirmTouched, setConfirmTouched] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // Mismatch is shown live once the user has left the confirm field, so they
  // aren't scolded while still typing.
  const passwordsMatch = password === passwordConfirm;
  const showMismatch = confirmTouched && passwordConfirm !== "" && !passwordsMatch;

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);

    if (!passwordsMatch) {
      setConfirmTouched(true);
      setError("Passwords don't match");
      return;
    }

    setLoading(true);

    try {
      // Firebase owns the credentials. The backend gets only the signed ID
      // token (identity) plus extra profile fields ΓÇö never email/uid in the
      // body, they are read from the verified token server-side.
      let credential: UserCredential;
      try {
        credential = await createUserWithEmailAndPassword(auth, email, password);
      } catch (err) {
        // The Firebase account can exist without our DB row (e.g. an earlier
        // signup died before reaching the backend). If the password matches,
        // sign in instead ΓÇö the backend signup below is idempotent and will
        // create the missing local user.
        if (err instanceof FirebaseError && err.code === "auth/email-already-in-use") {
          credential = await signInWithEmailAndPassword(auth, email, password);
        } else {
          throw err;
        }
      }
      const idToken = await credential.user.getIdToken();
      await authRequest("/api/auth/signup", idToken, { name, surname });
      // Prefill onboarding form with names we already collected here
      try {
        sessionStorage.setItem("fyo.signupName", name.trim());
        sessionStorage.setItem("fyo.signupSurname", surname.trim());
      } catch {
        /* private mode etc. */
      }
      window.location.hash = "#/onboarding";

    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong during signup");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="court auth">
      <header className="bar">
        <Wordmark onClick={goHome} />
        <nav className="bar__nav" aria-label="Primary">
          <a href="#/teams">Teams</a>
        </nav>
        <Button variant="ghost" className="bar__cta" onClick={goHome}>
          ΓåÉ Home
        </Button>
      </header>

      <main className="auth__shell">
        <p className="eyebrow">Join the court</p>
        <h1 className="section-title">Sign up</h1>

        <form className="auth__form" onSubmit={handleSubmit}>
          <div className="auth__row">
            <div className="auth__field">
              <label htmlFor="signup-name">First name</label>
              <input
                id="signup-name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
            <div className="auth__field">
              <label htmlFor="signup-surname">Last name</label>
              <input
                id="signup-surname"
                value={surname}
                onChange={(e) => setSurname(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="auth__field">
            <label htmlFor="signup-email">Email</label>
            <input
              id="signup-email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              aria-invalid={error ? true : undefined}
              aria-describedby={error ? "signup-error" : undefined}
            />
          </div>

          <div className="auth__field">
            <label htmlFor="signup-password">Password</label>
            <input
              id="signup-password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={6}
              aria-invalid={error ? true : undefined}
              aria-describedby={error ? "signup-error" : undefined}
            />
          </div>

          <div className="auth__field">
            <label htmlFor="signup-password-confirm">Confirm password</label>
            <input
              id="signup-password-confirm"
              type="password"
              value={passwordConfirm}
              onChange={(e) => setPasswordConfirm(e.target.value)}
              onBlur={() => setConfirmTouched(true)}
              required
              minLength={6}
              aria-invalid={showMismatch || undefined}
              aria-describedby={showMismatch ? "signup-password-confirm-hint" : undefined}
            />
            {showMismatch && (
              <p id="signup-password-confirm-hint" className="auth__error" role="alert">
                Passwords don't match
              </p>
            )}
          </div>

          {error && (
            <p id="signup-error" className="auth__error" role="alert">
              {error}
            </p>
          )}

          <Button variant="solid" type="submit" disabled={loading}>
            {loading ? "Signing upΓÇª" : "Sign up"}
          </Button>
        </form>

        <p className="auth__alt">
          Already have an account? <a href="#/login">Log in</a>
        </p>
      </main>
    </div>
  );
}
