import { useMemo } from "react";
import { TeamsBoard } from "./TeamsBoard";
import { useTeams } from "./useTeams";
import { Button, Wordmark } from "./ui";
import "./theme.css";
import "./teams.css";

const goHome = () => {
  window.location.hash = "#/";
};

/** Public, signed-out view of the team list. Signed-in users are routed to
 *  `#/app/teams`, which renders the same board inside the platform shell. */
export function TeamsView() {
  const { teams, loading, error, reload, applyRosterChange } = useTeams();

  const sportCount = useMemo(
    () => new Set(teams.map((t) => t.sport.name)).size,
    [teams]
  );

  const openSpotsTotal = useMemo(
    () => teams.reduce((sum, t) => sum + (t.isRecruiting ? t.openSpots : 0), 0),
    [teams]
  );

  return (
    <div className="court">
      <header className="bar">
        <Wordmark onClick={goHome} />
        <nav className="bar__nav" aria-label="Primary">
          <a href="#teams">Teams</a>
          <a href="#how">How it works</a>
        </nav>
        <Button variant="ghost" className="bar__cta" onClick={goHome}>
          Home
        </Button>
      </header>

      <section className="teamhero" id="how">
        <p className="eyebrow" data-reveal>
          Teams · Tbilisi &amp; beyond
        </p>
        <h1 className="section-title teamhero__title" data-reveal>
          Find a squad that needs you
        </h1>
        <p className="section-lead" data-reveal>
          Every team below is a captain looking for players right now. Filter by
          sport, see who's already in, and claim an open spot before it's gone.
        </p>

        <dl className="teamhero__stats" data-reveal>
          <div className="stat">
            <dt>Active teams</dt>
            <dd>{loading ? "—" : teams.length}</dd>
          </div>
          <div className="stat">
            <dt>Open spots</dt>
            <dd>{loading ? "—" : openSpotsTotal}</dd>
          </div>
          <div className="stat">
            <dt>Sports</dt>
            <dd>{loading ? "—" : sportCount}</dd>
          </div>
        </dl>
      </section>

      <section className="teams" id="teams">
        <TeamsBoard
          teams={teams}
          loading={loading}
          error={error}
          onRetry={reload}
          onRosterChange={applyRosterChange}
        />
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
