import { Wordmark } from "../common/Wordmark";
import { Button } from "../common/Button";
import { useAuth } from "../../hooks/useAuth";
import "./TopBar.css";

export function TopBar() {
  const { user, loading, signOut } = useAuth();

  async function handleSignOut() {
    await signOut();
    window.location.hash = "#/";
  }

  return (
    <header className="bar">
      <Wordmark href="#top" />
      <nav className="bar__nav" aria-label="Primary">
        <a href="#sports">Sports</a>
        <a href="#how">How it works</a>
        <a href="#/teams">Teams</a>
        {!loading && user ? (
          <>
            <a href="#/profile">Profile</a>
            <button type="button" className="bar__text-btn" onClick={handleSignOut}>
              Log out
            </button>
          </>
        ) : (
          <>
            <a href="#/login">Log in</a>
            <a href="#/signup">Sign up</a>
          </>
        )}
      </nav>
      <Button href="#join" className="bar__cta">
        Find a game
      </Button>
    </header>
  );
}
