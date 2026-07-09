import { useState } from "react";
import { signInWithEmailAndPassword } from "firebase/auth";
import { auth } from "./firebase";
import { authRequest, AuthApiError, type AuthUser } from "./authApi";
import { storeCurrentUserId } from "./auth/session";
import { Button, Wordmark } from "./teams/ui";
import "./teams/theme.css";
import "./teams/teams.css";
import "./auth.css";

const goHome = () => {
  window.location.hash = "#/";
};

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const credential = await signInWithEmailAndPassword(auth, email, password);
      const idToken = await credential.user.getIdToken();

      let authUser: AuthUser;
      try {
        authUser = await authRequest("/api/auth/login", idToken);
      } catch (err) {
        if (err instanceof AuthApiError && err.status === 404) {
          authUser = await authRequest("/api/auth/signup", idToken);
        } else {
          throw err;
        }
      }

      storeCurrentUserId(authUser.id);
      window.location.hash = "#/teams";
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong during login");
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
          Home
        </Button>
      </header>

      <main className="auth__shell">
        <p className="eyebrow">Welcome back</p>
        <h1 className="section-title">Log in</h1>

        <form className="auth__form" onSubmit={handleSubmit}>
          <div className="auth__field">
            <label htmlFor="login-email">Email</label>
            <input
              id="login-email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              aria-invalid={error ? true : undefined}
              aria-describedby={error ? "login-error" : undefined}
            />
          </div>

          <div className="auth__field">
            <label htmlFor="login-password">Password</label>
            <input
              id="login-password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              aria-invalid={error ? true : undefined}
              aria-describedby={error ? "login-error" : undefined}
            />
          </div>

          {error && (
            <p id="login-error" className="auth__error" role="alert">
              {error}
            </p>
          )}

          <Button variant="solid" type="submit" disabled={loading}>
            {loading ? "Logging in..." : "Log in"}
          </Button>
        </form>

        <p className="auth__alt">
          No account yet? <a href="#/signup">Sign up</a>
        </p>
      </main>
    </div>
  );
}