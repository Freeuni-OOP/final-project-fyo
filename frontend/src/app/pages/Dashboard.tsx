import { useEffect, useMemo, useState } from "react";
import { matchesApi, type Match, type MatchStatus } from "../../api/matches";
import { ApiError } from "../../api/http";
import { TeamRow } from "../../teams/TeamsBoard";
import { useTeams } from "../../teams/useTeams";
import { useReveal } from "../../teams/useReveal";
import { displayNameOf, useSession } from "../../session/SessionContext";
import { PageHead } from "../AppShell";
import { FriendRequestsPanel } from "../../friends/FriendRequestsPanel";

const STATUS_BADGE: Record<MatchStatus, string> = {
  UPCOMING: "badge--open",
  COMPLETED: "badge--closed",
  CANCELLED: "badge--cancelled",
};

const STATUS_LABEL: Record<MatchStatus, string> = {
  UPCOMING: "Upcoming",
  COMPLETED: "Played",
  CANCELLED: "Cancelled",
};

function formatKickoff(iso: string | null): string {
  if (!iso) return "Time to be agreed";
  try {
    return new Date(iso).toLocaleString("en-US", {
      weekday: "short",
      day: "numeric",
      month: "short",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return iso;
  }
}

/** Matches load independently of teams: a failure on one shouldn't blank the other. */
function useMyMatches(userId: number | undefined) {
  const [matches, setMatches] = useState<Match[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (userId === undefined) {
      setLoading(false);
      return;
    }
    let alive = true;
    setLoading(true);
    matchesApi
      .listForUser(userId)
      .then((m) => alive && setMatches(m))
      .catch((e: ApiError) => alive && setError(e.message))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [userId]);

  return { matches, loading, error };
}

export function Dashboard() {
  const { user } = useSession();
  const { teams, loading: teamsLoading } = useTeams();
  const { matches, loading: matchesLoading, error: matchesError } = useMyMatches(user?.id);

  const upcoming = useMemo(
    () => matches.filter((m) => m.status === "UPCOMING"),
    [matches]
  );

  const recruiting = useMemo(() => teams.filter((t) => t.isRecruiting), [teams]);

  const openSpotsTotal = useMemo(
    () => recruiting.reduce((sum, t) => sum + t.openSpots, 0),
    [recruiting]
  );

  useReveal([teamsLoading, matchesLoading, recruiting.length]);

  const loading = teamsLoading || matchesLoading;

  return (
    <>
      <PageHead
        eyebrow="Dashboard"
        title={<>Welcome back, {user ? displayNameOf(user) : "player"}</>}
      />

      <dl className="statrow" data-reveal>
        <div className="stat">
          <dt>Your upcoming matches</dt>
          <dd>{loading ? "—" : upcoming.length}</dd>
        </div>
        <div className="stat">
          <dt>Teams recruiting</dt>
          <dd>{loading ? "—" : recruiting.length}</dd>
        </div>
        <div className="stat">
          <dt>Open spots</dt>
          <dd>{loading ? "—" : openSpotsTotal}</dd>
        </div>
      </dl>

      <FriendRequestsPanel />

      <section className="panel">
        <div className="panel__head">
          <h2 className="panel__title">Your matches</h2>
        </div>

        {matchesLoading && <p className="teams__state">Loading matches…</p>}

        {matchesError && !matchesLoading && (
          <p className="teams__state">{matchesError}</p>
        )}

        {!matchesLoading && !matchesError && matches.length === 0 && (
          <p className="teams__state">
            No matches yet. Join a team and a captain will schedule one.
          </p>
        )}

        {!matchesLoading && !matchesError && matches.length > 0 && (
          <ul className="matchlist">
            {matches.slice(0, 5).map((m) => (
              <li className="match" key={m.id} data-reveal>
                <div>
                  <span className="match__sport">{m.sport.name}</span>
                  <p className="match__teams">
                    {m.home.displayName}
                    <span className="match__vs">vs</span>
                    {m.away.displayName}
                  </p>
                </div>
                <div className="match__meta">
                  {formatKickoff(m.proposedDatetime)}
                  {m.location ? ` · ${m.location}` : ""}
                </div>
                <span className={`badge ${STATUS_BADGE[m.status]}`}>
                  {STATUS_LABEL[m.status]}
                </span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="panel">
        <div className="panel__head">
          <h2 className="panel__title">Teams recruiting</h2>
          <a className="panel__link" href="#/app/teams">
            All teams →
          </a>
        </div>

        {teamsLoading && <p className="teams__state">Loading teams…</p>}

        {!teamsLoading && recruiting.length === 0 && (
          <p className="teams__state">No team is recruiting right now.</p>
        )}

        {!teamsLoading && recruiting.length > 0 && (
          <ul className="grid">
            {recruiting.slice(0, 5).map((t, i) => (
              <TeamRow key={t.id} team={t} index={i} href={`#/app/teams/${t.id}`} />
            ))}
          </ul>
        )}
      </section>
    </>
  );
}
