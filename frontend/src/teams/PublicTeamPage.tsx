import { TeamDetailPage } from "./TeamDetailPage";
import { Button, Wordmark } from "./ui";
import "./theme.css";
import "./teams.css";

const goHome = () => {
  window.location.hash = "#/";
};

/** `#/teams/:id` for signed-out visitors. Signed-in users get the same page
 *  inside the platform shell at `#/app/teams/:id`. */
export function PublicTeamPage({ teamId }: { teamId: number }) {
  return (
    <div className="court">
      <header className="bar">
        <Wordmark onClick={goHome} />
        <nav className="bar__nav" aria-label="Primary">
          <a href="#/teams">Teams</a>
          <a href="#/home">How it works</a>
        </nav>
        <Button variant="ghost" className="bar__cta" onClick={goHome}>
          Home
        </Button>
      </header>

      <section className="teams">
        <TeamDetailPage teamId={teamId} backHref="#/teams" />
      </section>

      <footer className="foot">
        <div className="foot__brand">
          <Wordmark onClick={goHome} />
          <p>Find your opponent. Tbilisi, Georgia.</p>
        </div>
        <p className="foot__fine">© 2026 FYO · A student project · Tbilisi, Georgia</p>
      </footer>
    </div>
  );
}
