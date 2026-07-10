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

export default function Signup() {
  const { signUp } = useSession();
  const [name, setName] = useState("");
  const [surname, setSurname] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [confirmTouched, setConfirmTouched] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

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
      // Prefill the onboarding form with names we already collected here.
      try {
        sessionStorage.setItem("fyo.signupName", name.trim());
        sessionStorage.setItem("fyo.signupSurname", surname.trim());
      } catch {
        /* private mode etc. */
      }

      // Firebase owns the credentials. The backend gets only the signed ID
      // token (identity) plus extra profile fields — never email/uid in the
      // body, they are read from the verified token server-side. The new
      // account has `onboarding: true`, so App routes it to `#/onboarding`.
      await signUp({ email, password, name, surname });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong during signup");
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
            <div className="auth__password">
              <input
                id="signup-password"
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength={6}
                aria-invalid={error ? true : undefined}
                aria-describedby={error ? "signup-error" : undefined}
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

          <div className="auth__field">
            <label htmlFor="signup-password-confirm">Confirm password</label>
            <div className="auth__password">
              <input
                id="signup-password-confirm"
                type={showConfirmPassword ? "text" : "password"}
                value={passwordConfirm}
                onChange={(e) => setPasswordConfirm(e.target.value)}
                onBlur={() => setConfirmTouched(true)}
                required
                minLength={6}
                aria-invalid={showMismatch || undefined}
                aria-describedby={showMismatch ? "signup-password-confirm-hint" : undefined}
              />

              <button
                type="button"
                className="auth__toggle"
                onClick={() => setShowConfirmPassword((v) => !v)}
                aria-label={showConfirmPassword ? "Hide password" : "Show password"}
              >
                {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
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
            {loading ? "Signing up" : "Sign up"}
          </Button>
        </form>

        <p className="auth__alt">
          Already have an account? <a href="#/login">Log in</a>
        </p>
      </main>
    </div>
  );
}