import { useEffect, useMemo, useState } from "react";
import { ApiError, teamsApi } from "./api";
import type { TeamDetails, TeamSummary } from "./types";
import { TeamDetail } from "./TeamDetail";
import { useReveal } from "./useReveal";
import { Button, Wordmark } from "./ui";
import "./theme.css";
import "./teams.css";

type Filter = "ALL" | "RECRUITING";

const goHome = () => {
  window.location.hash = "#/";
};

export function TeamsView() {
  const [teams, setTeams] = useState<TeamSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [sport, setSport] = useState<string>("ALL");
  const [status, setStatus] = useState<Filter>("ALL");
  const [openId, setOpenId] = useState<number | null>(null);

  function load() {
    setLoading(true);
    setError(null);
    teamsApi
      .list()
      .then(setTeams)
      .catch((e: ApiError) => setError(e.message))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  const sports = useMemo(
    () => Array.from(new Set(teams.map((t) => t.sport.name))).sort(),
    [teams]
  );

  const visible = useMemo(
    () =>
      teams.filter(
        (t) =>
          (sport === "ALL" || t.sport.name === sport) &&
          (status === "ALL" || t.isRecruiting)
      ),
    [teams, sport, status]
  );

  const openSpotsTotal = useMemo(
    () => teams.reduce((sum, t) => sum + (t.isRecruiting ? t.openSpots : 0), 0),
    [teams]
  );

  // Reveal animations re-bind whenever the visible set changes.
  useReveal([visible.length, loading]);

  /** Keep the list row in sync after someone joins from the drawer. */
  function handleJoined(updated: TeamDetails) {
    setTeams((prev) =>
      prev.map((t) =>
        t.id === updated.id
          ? { ...t, openSpots: updated.openSpots, isRecruiting: updated.isRecruiting }
          : t
      )
    );
  }

  return (
    <div className="court">
      <header className="bar">
        <Wordmark onClick={goHome} />
        <nav className="bar__nav" aria-label="Primary">
          <a href="#teams">Teams</a>
          <a href="#how">How it works</a>
        </nav>
        <Button variant="ghost" className="bar__cta" onClick={goHome}>
          ← Home
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
            <dd>{loading ? "—" : sports.length}</dd>
          </div>
        </dl>
      </section>

      <section className="teams" id="teams">
        <div className="teams__controls" data-reveal>
          <div className="chips" role="group" aria-label="Filter by sport">
            <button
              className={`chip ${sport === "ALL" ? "chip--on" : ""}`}
              onClick={() => setSport("ALL")}
            >
              All sports
            </button>
            {sports.map((s) => (
              <button
                key={s}
                className={`chip ${sport === s ? "chip--on" : ""}`}
                onClick={() => setSport(s)}
              >
                {s}
              </button>
            ))}
          </div>
          <button
            className={`toggle ${status === "RECRUITING" ? "toggle--on" : ""}`}
            onClick={() =>
              setStatus((s) => (s === "RECRUITING" ? "ALL" : "RECRUITING"))
            }
            aria-pressed={status === "RECRUITING"}
          >
            <span className="toggle__dot" />
            Recruiting only
          </button>
        </div>

        {loading && <p className="teams__state">Loading teams…</p>}

        {error && !loading && (
          <div className="teams__state teams__state--error">
            <p>{error}</p>
            <Button variant="ghost" onClick={load}>
              Try again
            </Button>
          </div>
        )}

        {!loading && !error && visible.length === 0 && (
          <p className="teams__state">No teams match that filter yet.</p>
        )}

        {!loading && !error && visible.length > 0 && (
          <ul className="grid">
            <li className="grid__head">
              <span>Team</span>
              <span className="grid__head-mid">Captain</span>
              <span className="grid__head-spots">Open spots</span>
              <span />
            </li>
            {visible.map((t, i) => {
              const filled = t.maxPlayers - t.openSpots;
              const pct = Math.round((filled / t.maxPlayers) * 100);
              return (
                <li
                  className="row"
                  key={t.id}
                  data-reveal
                  onClick={() => setOpenId(t.id)}
                  style={{ transitionDelay: `${Math.min(i, 8) * 40}ms` }}
                >
                  <div className="row__team">
                    <span className="row__no">
                      {String(i + 1).padStart(2, "0")}
                    </span>
                    <div className="row__team-id">
                      <span className="row__sport">{t.sport.name}</span>
                      <span className="row__name">{t.name}</span>
                      <span className="row__region">{t.region ?? "—"}</span>
                    </div>
                  </div>

                  <div className="row__captain">
                    <span className="row__captain-label">Captain</span>
                    {t.captain.name} {t.captain.surname}
                  </div>

                  <div className="row__spots">
                    <span className="meter__track">
                      <span
                        className="meter__fill"
                        style={{ width: `${pct}%` }}
                      />
                    </span>
                    <span className="row__spots-n">
                      {t.isRecruiting ? (
                        <>
                          <strong>{t.openSpots}</strong> open
                        </>
                      ) : (
                        "Full"
                      )}
                    </span>
                  </div>

                  <span className="row__go" aria-hidden="true">
                    View →
                  </span>
                </li>
              );
            })}
          </ul>
        )}
      </section>

      <footer className="foot">
        <div className="foot__brand">
          <Wordmark onClick={goHome} />
          <p>Find your game. Tbilisi, Georgia.</p>
        </div>
        <p className="foot__fine">© 2026 FYO · A student project · Tbilisi, Georgia</p>
      </footer>

      {openId !== null && (
        <TeamDetail
          teamId={openId}
          onClose={() => setOpenId(null)}
          onJoined={handleJoined}
        />
      )}
    </div>
  );
}
