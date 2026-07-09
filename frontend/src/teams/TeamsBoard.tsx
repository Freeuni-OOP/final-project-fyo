import { useMemo, useState } from "react";
import type { TeamDetails, TeamSummary } from "./types";
import { TeamDetail } from "./TeamDetail";
import { useReveal } from "./useReveal";
import { Button } from "./ui";

type Filter = "ALL" | "RECRUITING";

interface TeamsBoardProps {
  teams: TeamSummary[];
  loading: boolean;
  error: string | null;
  onRetry: () => void;
  onRosterChange: (team: TeamDetails) => void;
  currentUserId?: number;
}

/** Sport/recruiting filters, the fixtures list, and the detail drawer.
 *  Shared by the public `#/teams` page and the in-shell `#/app/teams` page. */
export function TeamsBoard({
  teams,
  loading,
  error,
  onRetry,
  onRosterChange,
  currentUserId,
}: TeamsBoardProps) {
  const [sport, setSport] = useState<string>("ALL");
  const [status, setStatus] = useState<Filter>("ALL");
  const [openId, setOpenId] = useState<number | null>(null);

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

  useReveal([visible.length, loading]);

  return (
    <>
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
          onClick={() => setStatus((s) => (s === "RECRUITING" ? "ALL" : "RECRUITING"))}
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
          <Button variant="ghost" onClick={onRetry}>
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
          {visible.map((t, i) => (
            <TeamRow key={t.id} team={t} index={i} onOpen={() => setOpenId(t.id)} />
          ))}
        </ul>
      )}

      {openId !== null && (
        <TeamDetail
          teamId={openId}
          onClose={() => setOpenId(null)}
          onJoined={onRosterChange}
          currentUserId={currentUserId}
        />
      )}
    </>
  );
}

interface TeamRowProps {
  team: TeamSummary;
  index: number;
  onOpen: () => void;
}

/** One fixtures-style row. Also used by the dashboard's recruiting panel. */
export function TeamRow({ team, index, onOpen }: TeamRowProps) {
  const filled = team.maxPlayers - team.openSpots;
  const pct = Math.round((filled / team.maxPlayers) * 100);

  return (
    <li
      className="row"
      data-reveal
      onClick={onOpen}
      style={{ transitionDelay: `${Math.min(index, 8) * 40}ms` }}
    >
      <div className="row__team">
        <span className="row__no">{String(index + 1).padStart(2, "0")}</span>
        <div className="row__team-id">
          <span className="row__sport">{team.sport.name}</span>
          <span className="row__name">{team.name}</span>
          <span className="row__region">{team.region ?? "—"}</span>
        </div>
      </div>

      <div className="row__captain">
        <span className="row__captain-label">Captain</span>
        {team.captain.name} {team.captain.surname}
      </div>

      <div className="row__spots">
        <span className="meter__track">
          <span className="meter__fill" style={{ width: `${pct}%` }} />
        </span>
        <span className="row__spots-n">
          {team.isRecruiting ? (
            <>
              <strong>{team.openSpots}</strong> open
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
}
