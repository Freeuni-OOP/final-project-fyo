import { Wordmark } from "../common/Wordmark";
import { Button } from "../common/Button";
import "./TopBar.css";

export function TopBar() {
  return (
    <header className="bar">
      <Wordmark href="#top" />
      <nav className="bar__nav" aria-label="Primary">
        <a href="#sports">Sports</a>
        <a href="#how">How it works</a>
        <a href="#teams">Teams</a>
      </nav>
      <Button href="#join" className="bar__cta">
        Find a game
      </Button>
    </header>
  );
}
