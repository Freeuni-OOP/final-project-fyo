import { useState } from "react";
import { createUserWithEmailAndPassword } from "firebase/auth";
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
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      // Firebase owns the credentials. The backend gets only the signed ID
      // token (identity) plus extra profile fields — never email/uid in the
      // body, they are read from the verified token server-side.
      const credential = await createUserWithEmailAndPassword(auth, email, password);
      const idToken = await credential.user.getIdToken();
      await authRequest("/api/auth/signup", idToken, { name, surname });
      window.location.hash = "#/teams";
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
          ← Home
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
            />
          </div>

          {error && <p className="auth__error">{error}</p>}

          <Button variant="solid" type="submit" disabled={loading}>
            {loading ? "Signing up…" : "Sign up"}
          </Button>
        </form>

        <p className="auth__alt">
          Already have an account? <a href="#/login">Log in</a>
        </p>
      </main>
    </div>
  );
}
