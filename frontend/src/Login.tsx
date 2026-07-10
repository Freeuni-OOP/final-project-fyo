import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { useSession } from "./session/SessionContext";
import { Button, Wordmark } from "./teams/ui";
import "./teams/theme.css";
import "./teams/teams.css";
import "./auth.css";

const goHome = () => {
  window.location.hash = "#/";
};

export default function Login() {
  const { signIn, error: sessionError } = useSession();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // A session that failed to restore lands the user here carrying its reason
  // (e.g. a Firebase identity with no account). This attempt's error wins.
  const shownError = error ?? sessionError;

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
          <a href="#/profile">Profile</a>
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
              aria-invalid={shownError ? true : undefined}
              aria-describedby={shownError ? "login-error" : undefined}
            />
          </div>

          <div className="auth__field">
            <label htmlFor="login-password">Password</label>
            <div className="auth__password">
              <input
                id="login-password"
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                aria-invalid={shownError ? true : undefined}
                aria-describedby={shownError ? "login-error" : undefined}
              />

              <button
                type="button"
                className="auth__toggle"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? "Hide password" : "Show password"}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          {shownError && (
            <p id="login-error" className="auth__error" role="alert">
              {shownError}
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