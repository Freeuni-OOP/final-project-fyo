import { useEffect, useMemo, useState } from "react";
import { ApiError, matchesApi } from "./api";
import type { Match, MatchStatus } from "./types";
import { MatchDetail } from "./MatchDetail";
import { useReveal } from "../teams/useReveal";
import { Button, Wordmark } from "../teams/ui";
import "../teams/theme.css";
import "../teams/teams.css";
import "./matches.css";

type StatusFilter = "ALL" | MatchStatus;

const goHome = () => {
  window.location.hash = "#/";
};

function formatWhen(iso: string | null): string {
  if (!iso) return "TBD";
  try {
    return new Date(iso).toLocaleString("en-US", {
      day: "numeric",
      month: "short",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return iso;
  }
}

function formatLabel(format: Match["format"]): string {
  return format === "ONE_VS_ONE" ? "1v1" : "Team";
}

export function MatchesView() {
  const [matches, setMatches] = useState<Match[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [sport, setSport] = useState<string>("ALL");
  const [status, setStatus] = useState<StatusFilter>("ALL");
  const [openId, setOpenId] = useState<number | null>(null);

  function load() {
    setLoading(true);
    setError(null);
    matchesApi
      .list()
      .then(setMatches)
      .catch((e: ApiError) => setError(e.message))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  const sports = useMemo(
    () => Array.from(new Set(matches.map((m) => m.sport.name))).sort(),
    [matches]
  );

  const visible = useMemo(
    () =>
      matches.filter(
        (m) =>
          (sport === "ALL" || m.sport.name === sport) &&
          (status === "ALL" || m.status === status)
      ),
    [matches, sport, status]
  );

  const upcomingCount = useMemo(
    () => matches.filter((m) => m.status === "UPCOMING").length,
    [matches]
  );

  useReveal([visible.length, loading]);

  function handleUpdated(updated: Match) {
    setMatches((prev) =>
      prev.map((m) => (m.id === updated.id ? updated : m))
    );
  }

  return (
    <div className="court">
      <header className="bar">
        <Wordmark onClick={goHome} />
        <nav className="bar__nav" aria-label="Primary">
          <a href="#/teams">Teams</a>
          <a href="#matches">Matches</a>
        </nav>
        <Button variant="ghost" className="bar__cta" onClick={goHome}>
          ← Home
        </Button>
      </header>

      <section className="teamhero" id="matches">
        <p className="eyebrow" data-reveal>
          Matches · locked in
        </p>
        <h1 className="section-title teamhero__title" data-reveal>
          Who's playing who
        </h1>
        <p className="section-lead" data-reveal>
          Confirmed fixtures from the API. Filter by sport or status, open a
          match for details, and cancel if you're a participant (or captain).
        </p>

        <dl className="teamhero__stats" data-reveal>
          <div className="stat">
            <dt>All matches</dt>
            <dd>{loading ? "—" : matches.length}</dd>
          </div>
          <div className="stat">
            <dt>Upcoming</dt>
            <dd>{loading ? "—" : upcomingCount}</dd>
          </div>
          <div className="stat">
            <dt>Sports</dt>
            <dd>{loading ? "—" : sports.length}</dd>
          </div>
        </dl>
      </section>

      <section className="teams">
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
          <div className="chips" role="group" aria-label="Filter by status">
            {(
              [
                ["ALL", "All"],
                ["UPCOMING", "Upcoming"],
                ["COMPLETED", "Done"],
                ["CANCELLED", "Cancelled"],
              ] as const
            ).map(([value, label]) => (
              <button
                key={value}
                className={`chip ${status === value ? "chip--on" : ""}`}
                onClick={() => setStatus(value)}
              >
                {label}
              </button>
            ))}
          </div>
        </div>

        {loading && <p className="teams__state">Loading matches…</p>}

        {error && !loading && (
          <div className="teams__state teams__state--error">
            <p>{error}</p>
            <Button variant="ghost" onClick={load}>
              Try again
            </Button>
          </div>
        )}

        {!loading && !error && visible.length === 0 && (
          <p className="teams__state">No matches for that filter yet.</p>
        )}

        {!loading && !error && visible.length > 0 && (
          <ul className="grid">
            <li className="grid__head">
              <span>Matchup</span>
              <span className="grid__head-mid">When</span>
              <span className="grid__head-spots">Status</span>
              <span />
            </li>
            {visible.map((m, i) => (
              <li
                className="row"
                key={m.id}
                data-reveal
                onClick={() => setOpenId(m.id)}
                style={{ transitionDelay: `${Math.min(i, 8) * 40}ms` }}
              >
                <div className="row__team">
                  <span className="row__no">
                    {String(i + 1).padStart(2, "0")}
                  </span>
                  <div className="row__team-id">
                    <span className="row__sport">
                      {m.sport.name} · {formatLabel(m.format)}
                    </span>
                    <span className="row__name match-row__vs">
                      {m.home.displayName}
                      <span className="match-row__sep"> vs </span>
                      {m.away.displayName}
                    </span>
                    <span className="row__region">{m.location ?? "—"}</span>
                  </div>
                </div>

                <div className="row__captain">
                  <span className="row__captain-label">Kickoff</span>
                  {formatWhen(m.proposedDatetime)}
                </div>

                <div className="row__spots">
                  <span
                    className={`match-status match-status--${m.status.toLowerCase()}`}
                  >
                    {m.status}
                  </span>
                </div>

                <span className="row__go" aria-hidden="true">
                  View →
                </span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <footer className="foot">
        <div className="foot__brand">
          <Wordmark onClick={goHome} />
          <p>Find your game. Tbilisi, Georgia.</p>
        </div>
        <p className="foot__fine">
          © 2026 FYO · A student project · Tbilisi, Georgia
        </p>
      </footer>

      {openId !== null && (
        <MatchDetail
          matchId={openId}
          onClose={() => setOpenId(null)}
          onUpdated={handleUpdated}
        />
      )}
    </div>
  );
}
