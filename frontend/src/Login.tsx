import { useState } from "react";
import { useSession } from "./session/SessionContext";
import { Button, Wordmark } from "./teams/ui";
import "./teams/theme.css";
import "./teams/teams.css";
import "./auth.css";

const goHome = () => {
  window.location.hash = "#/";
};

export default function Login() {
  const { signIn } = useSession();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      // Firebase checks the password, the session exchanges the ID token for
      // our local user, and App redirects off `#/login` once it lands.
      await signIn(email, password);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong during login");
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
          ← Home
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
