import { Wordmark } from "../teams/ui";
import "../teams/theme.css";
import "../teams/teams.css";
import "./AppShell.css";

/** Held while the Firebase handshake resolves, so signed-in users never see the landing page flash. */
export function Splash() {
  return (
    <div className="court splash">
      <Wordmark />
      <p className="splash__text">Loading…</p>
    </div>
  );
}
