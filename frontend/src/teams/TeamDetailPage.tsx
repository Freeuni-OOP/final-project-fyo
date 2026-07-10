import { useEffect, useState } from "react";
import { useAuth } from "../hooks/useAuth";
import { openTeamChat } from "../friends/openDirectChat";
import { ApiError, teamsApi } from "./api";
import { TeamJoinPanel } from "./TeamJoinPanel";
import type { TeamDetails, JoinRequest } from "./types";
import { Avatar, Ball, Button } from "./ui";

interface TeamDetailPageProps {
  teamId: number;
  /** Where "All teams" goes back to — differs between the public and shell views. */
  backHref: string;
  currentUserId?: number;
}

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

export function TeamDetailPage({ teamId, backHref, currentUserId }: TeamDetailPageProps) {
  const { getIdToken } = useAuth();
  const [team, setTeam] = useState<TeamDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [openingChat, setOpeningChat] = useState(false);

  const [pendingRequests, setPendingRequests] = useState<JoinRequest[]>([]);
  const [loadingRequests, setLoadingRequests] = useState(false);

  useEffect(() => {
    let alive = true;
    setLoading(true);
    setError(null);
    teamsApi
      .get(teamId)
      .then((t) => alive && setTeam(t))
      .catch((e: ApiError) => alive && setError(e.message))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [teamId]);

  useEffect(() => {
    if (!team || !currentUserId || team.captain.id !== currentUserId) return;
    let alive = true;
    setLoadingRequests(true);
    teamsApi
      .getPendingRequests(teamId)
      .then((t) => alive && setPendingRequests(t))
      .catch(() => {})
      .finally(() => alive && setLoadingRequests(false));

    return () => {
      alive = false;
    };
  }, [team, teamId, currentUserId]);

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [teamId]);

  async function handleAccept(requestId: number) {
    try {
      await teamsApi.acceptRequest(teamId, requestId, currentUserId!);
      setPendingRequests((prev) => prev.filter((r) => r.id !== requestId));
      setTeam(await teamsApi.get(teamId));
    } catch (err) {
      alert((err as ApiError).message);
    }
  }

  async function handleDecline(requestId: number) {
    try {
      await teamsApi.declineRequest(teamId, requestId, currentUserId!);
      setPendingRequests((prev) => prev.filter((r) => r.id !== requestId));
    } catch (err) {
      alert((err as ApiError).message);
    }
  }

  const filled = team ? team.maxPlayers - team.openSpots : 0;
  const pct = team ? Math.round((filled / team.maxPlayers) * 100) : 0;
  const isCaptain = team && currentUserId && team.captain.id === currentUserId;
  const isMember =
    team &&
    currentUserId &&
    (team.captain.id === currentUserId ||
      team.members.some((m) => m.userId === currentUserId));

  async function handleOpenTeamChat() {
    setOpeningChat(true);
    try {
      const token = await getIdToken();
      if (!token) throw new Error("Your session expired. Sign in again.");
      await openTeamChat(token, teamId);
    } catch (err) {
      alert(err instanceof Error ? err.message : "Could not open team chat.");
    } finally {
      setOpeningChat(false);
    }
  }

  return (
    <article className="teampage">
      <a className="teampage__back" href={backHref}>
        ← All teams
      </a>

      {loading && <p className="teampage__state">Loading team…</p>}

      {error && !loading && (
        <div className="teampage__state teampage__state--error">
          <p>{error}</p>
          <Button variant="ghost" onClick={() => (window.location.hash = backHref)}>
            Back to teams
          </Button>
        </div>
      )}

      {team && !loading && (
        <>
          <header className="td__head">
            <div className="td__logo">
              {team.logoUrl ? (
                <img src={team.logoUrl} alt="" />
              ) : (
                <Ball className="td__logo-ball" />
              )}
            </div>
            <div className="td__heading">
              <span className="td__sport">{team.sport.name}</span>
              <h1 className="td__name">{team.name}</h1>
              <p className="td__meta">
                {team.region ?? "—"} · founded {formatDate(team.createdAt)}
              </p>
            </div>
          </header>

          <span className={`badge ${team.isRecruiting ? "badge--open" : "badge--closed"}`}>
            {team.isRecruiting ? "Recruiting now" : "Roster closed"}
          </span>

          {team.description && <p className="td__desc">{team.description}</p>}

          {isMember && (
            <p className="td__desc">
              <Button variant="ghost" onClick={() => void handleOpenTeamChat()} disabled={openingChat}>
                {openingChat ? "Opening chat…" : "Open team chat →"}
              </Button>
            </p>
          )}

          <div className="td__roster-meta">
            <div className="meter">
              <span className="meter__track">
                <span className="meter__fill" style={{ width: `${pct}%` }} />
              </span>
            </div>
            <p className="td__count">
              <strong>{filled}</strong> / {team.maxPlayers} players ·{" "}
              <span className="td__open">{team.openSpots} open</span>
            </p>
          </div>

          <section className="td__block">
            <h2 className="td__block-title">Captain</h2>
            <div className="player player--captain">
              <Avatar
                src={team.captain.imageUrl}
                name={`${team.captain.name} ${team.captain.surname}`}
                size={48}
              />
              <div className="player__id">
                <span className="player__name">
                  {team.captain.name} {team.captain.surname}
                </span>
                <span className="player__handle">
                  @{team.captain.username}
                  {team.captain.region ? ` · ${team.captain.region}` : ""}
                </span>
              </div>
              <span className="player__role player__role--captain">Captain</span>
            </div>
          </section>

          <section className="td__block">
            <h2 className="td__block-title">
              Roster <span className="td__block-n">{team.members.length}</span>
            </h2>
            <ul className="roster">
              {team.members.map((m) => (
                <li className="player" key={m.id}>
                  <Avatar src={m.imageUrl} name={m.fullName} size={40} />
                  <div className="player__id">
                    <span className="player__name">{m.fullName}</span>
                    <span className="player__handle">
                      @{m.username} · joined {formatDate(m.joinedAt)}
                    </span>
                  </div>
                  <span
                    className={`player__role ${
                      m.role === "CAPTAIN" ? "player__role--captain" : ""
                    }`}
                  >
                    {m.role === "CAPTAIN" ? "Captain" : "Member"}
                  </span>
                </li>
              ))}
            </ul>
          </section>

          {isCaptain && (
            <section className="td__block">
              <h2 className="td__block-title">
                Join Requests <span className="td__block-n">{pendingRequests.length}</span>
              </h2>
              {loadingRequests && <p className="teampage__state">Loading requests…</p>}
              {!loadingRequests && pendingRequests.length === 0 && (
                <p className="td__closed-note">No pending requests.</p>
              )}
              {!loadingRequests && pendingRequests.length > 0 && (
                <ul className="roster">
                  {pendingRequests.map((r) => (
                    <li className="player" key={r.id}>
                      <Avatar src={r.imageUrl} name={r.fullName} size={40} />
                      <div className="player__id">
                        <span className="player__name">{r.fullName}</span>
                        <span className="player__handle">@{r.username}</span>
                      </div>
                      <div className="jr__actions">
                        <button
                          className="jr__btn jr__btn--accept"
                          onClick={() => handleAccept(r.id)}
                          aria-label={`Accept ${r.username}`}
                        >
                          ✓
                        </button>
                        <button
                          className="jr__btn jr__btn--decline"
                          onClick={() => handleDecline(r.id)}
                          aria-label={`Decline ${r.username}`}
                        >
                          ✕
                        </button>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </section>
          )}

          <footer className="td__foot">
            <TeamJoinPanel team={team} currentUserId={currentUserId} />
          </footer>
        </>
      )}
    </article>
  );
}
