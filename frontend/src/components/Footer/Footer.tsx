import { Wordmark } from "../common/Wordmark";
import "./Footer.css";

export function Footer() {
  return (
    <footer className="foot">
      <div className="foot__brand">
        <Wordmark />
        <p>Find your game. Tbilisi, Georgia.</p>
      </div>

      <div className="foot__cols">
        <div className="foot__col">
          <span className="foot__h">Play</span>
          <a href="#sports">Sports</a>
          <a href="#how">How it works</a>
          <a href="#join">Find a game</a>
        </div>
        <div className="foot__col">
          <span className="foot__h">Teams</span>
          <a href="#teams">For captains</a>
          <a href="#teams">Roster fill</a>
          <a href="#how">Match reports</a>
        </div>
        <div className="foot__col">
          <span className="foot__h">FYO</span>
          <a href="#top">About</a>
          <a href="#top">Cities</a>
          <a href="#top">Contact</a>
        </div>
      </div>

      <p className="foot__fine">
        © 2026 FYO · A student project · Tbilisi, Georgia
      </p>
    </footer>
  );
}
