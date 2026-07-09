import { Wordmark } from "../common/Wordmark";
import { Button } from "../common/Button";
import { useSession } from "../../session/SessionContext";
import "./TopBar.css";

export function TopBar() {
  const { status } = useSession();
  const authed = status === "authed";

  return (
    <header className="bar">
      <Wordmark href="#top" />
      <nav className="bar__nav" aria-label="Primary">
        <a href="#sports">Sports</a>
        <a href="#how">How it works</a>
        <a href={authed ? "#/app/teams" : "#/teams"}>Teams</a>
        {authed && <a href="#/chat">Chat</a>}
        {!authed && <a href="#/login">Log in</a>}
        {!authed && <a href="#/signup">Sign up</a>}
      </nav>
      <Button href={authed ? "#/app" : "#join"} className="bar__cta">
        {authed ? "Go to app" : "Find a game"}
      </Button>
    </header>
  );
}
