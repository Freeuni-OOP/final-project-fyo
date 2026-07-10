import { useMemo } from "react";
import { TeamRow } from "../../teams/TeamsBoard";
import { useMyTeams } from "../../teams/useMyTeams";
import { useReveal } from "../../teams/useReveal";
import { useSession } from "../../session/SessionContext";
import { Button } from "../../teams/ui";
import { PageHead } from "../AppShell";

function formatDate(iso: string): string {
  try {
    return new Date(iso).toLocaleDateString("en-US", {
      day: "numeric",
      month: "short",
      year: "numeric",
    });
  } catch {
    return iso;
  }
}

/** The teams the signed-in user plays for, plus the requests they're waiting on. */
export function MyTeamsPage() {
  const { user } = useSession();
  const { teams, requests, loading, error, reload } = useMyTeams(user?.id);

  const captained = useMemo(
    () => teams.filter((t) => t.role === "CAPTAIN").length,
    [teams]
  );
  const pending = useMemo(
    () => requests.filter((r) => r.status === "PENDING").length,
    [requests]
  );

  useReveal([loading, teams.length, requests.length]);

  return (
    <>
      <PageHead
        eyebrow="Teams · your squads"
        title="Your teams"
        actions={
          <Button variant="ghost" onClick={() => (window.location.hash = "#/app/teams")}>
            Browse teams
          </Button>
        }
      />

      <dl className="statrow" data-reveal>
        <div className="stat">
          <dt>Teams joined</dt>
          <dd>{loading ? "—" : teams.length}</dd>
        </div>
        <div className="stat">
          <dt>Teams you captain</dt>
          <dd>{loading ? "—" : captained}</dd>
        </div>
        <div className="stat">
          <dt>Requests pending</dt>
          <dd>{loading ? "—" : pending}</dd>
        </div>
      </dl>

      {error && !loading && (
        <div className="teams__state teams__state--error">
          <p>{error}</p>
          <Button variant="ghost" onClick={reload}>
            Try again
          </Button>
        </div>
      )}

      {!error && (
        <>
          <section className="panel">
            <div className="panel__head">
              <h2 className="panel__title">Teams you play for</h2>
            </div>

            {loading && <p className="teams__state">Loading your teams…</p>}

            {!loading && teams.length === 0 && (
              <p className="teams__state">
                You haven't joined a team yet.{" "}
                <a className="panel__link" href="#/app/teams">
                  Find one →
                </a>
              </p>
            )}

            {!loading && teams.length > 0 && (
              <ul className="grid">
                {teams.map((myTeam, i) => (
                  <TeamRow
                    key={myTeam.team.id}
                    team={myTeam.team}
                    index={i}
                    tag={myTeam.role === "CAPTAIN" ? "Captain" : "Member"}
                    href={`#/app/teams/${myTeam.team.id}`}
                  />
                ))}
              </ul>
            )}
          </section>

          {!loading && requests.length > 0 && (
            <section className="panel">
              <div className="panel__head">
                <h2 className="panel__title">Requests you've sent</h2>
              </div>

              <ul className="reqlist">
                {requests.map((request) => (
                  <li className="req" key={request.id} data-reveal>
                    <a className="req__team" href={`#/app/teams/${request.team.id}`}>
                      <span className="req__sport">{request.team.sport.name}</span>
                      <span className="req__name">{request.team.name}</span>
                    </a>
                    <span className="req__meta">sent {formatDate(request.createdAt)}</span>
                    <span
                      className={`badge ${
                        request.status === "PENDING" ? "badge--open" : "badge--cancelled"
                      }`}
                    >
                      {request.status === "PENDING" ? "Pending" : "Declined"}
                    </span>
                  </li>
                ))}
              </ul>
            </section>
          )}
        </>
      )}
    </>
  );
}
