import { useEffect, useState } from "react";
import { ApiError, teamsApi } from "./api";
import type { TeamDetails, JoinRequest } from "./types";
import { Avatar, Ball, Button } from "./ui";
import { useAuth } from "../hooks/useAuth";

interface TeamDetailProps {
  teamId: number;
  onClose: () => void;
  /** Bubble an updated roster up so the list view stays in sync after a join. */
  onJoined?: (team: TeamDetails) => void;
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

export function TeamDetail({ teamId, onClose, onJoined, currentUserId }: TeamDetailProps) {
  const [team, setTeam] = useState<TeamDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [requestOpen, setRequestOpen] = useState(false);
  // Signed-in callers already know who they are; only the public view has to ask.
  const [userId, setUserId] = useState(currentUserId ? String(currentUserId) : "");
  const [requesting, setRequesting] = useState(false);
  const [requestError, setRequestError] = useState<string | null>(null);
  const [requested, setRequested] = useState(false);

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
    setLoadingRequests(true);
    getIdToken().then(token => {
        if (!token) return;
        teamsApi.getPendingRequests(teamId, token)
          .then(setPendingRequests)
          .catch(() => {})
          .finally(() => setLoadingRequests(false));
      });
  }, [team, teamId, currentUserId]);

  // Close on Escape; lock body scroll while the drawer is open.
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => e.key === "Escape" && onClose();
    document.addEventListener("keydown", onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = prev;
    };
  }, [onClose]);

  async function submitRequest(e: React.FormEvent) {
    e.preventDefault();
    const id = Number(userId);
    if (!Number.isInteger(id) || id <= 0) {
      setRequestError("Enter a valid numeric player id.");
      return;
    }
    setRequesting(true);
    setRequestError(null);
    try {
      const token = await getIdToken();
      if (!token) throw new Error("You must be signed in to request to join.");
      await teamsApi.requestToJoin(teamId, id);
      setRequested(true);
      setRequestOpen(false);
      setUserId("");
    } catch (err) {
      setRequestError((err as ApiError).message);
    } finally {
      setRequesting(false);
    }
  }

  async function handleAccept(requestId: number) {
    try {
      const token = await getIdToken();
      if (!token) return;
      await teamsApi.acceptRequest(teamId, requestId, token);
      setPendingRequests((prev) => prev.filter((r) => r.id !== requestId));
      const updated = await teamsApi.get(teamId);
      setTeam(updated);
      onJoined?.(updated);
    } catch (err) {
      alert((err as ApiError).message);
    }
  }

  async function handleDecline(requestId: number) {
    try {
      const token = await getIdToken();
      if (!token) return;
      await teamsApi.declineRequest(teamId, requestId, token);
      setPendingRequests((prev) => prev.filter((r) => r.id !== requestId));
    } catch (err) {
      alert((err as ApiError).message);
    }
  }

  const filled = team ? team.maxPlayers - team.openSpots : 0;
  const pct = team ? Math.round((filled / team.maxPlayers) * 100) : 0;
  const isCaptain = team && currentUserId && team.captain.id === currentUserId;

  return (
    <div className="drawer" role="dialog" aria-modal="true" aria-label="Team details">
      <div className="drawer__scrim" onClick={onClose} />
      <aside className="drawer__panel">
        <button className="drawer__close" onClick={onClose} aria-label="Close">
          ✕
        </button>

        {loading && <p className="drawer__state">Loading team…</p>}
        {error && !loading && (
          <div className="drawer__state drawer__state--error">
            <p>{error}</p>
            <Button variant="ghost" onClick={onClose}>
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
                <h2 className="td__name">{team.name}</h2>
                <p className="td__meta">
                  {team.region ?? "—"} · founded {formatDate(team.createdAt)}
                </p>
              </div>
            </header>

            <span
              className={`badge ${team.isRecruiting ? "badge--open" : "badge--closed"}`}
            >
              {team.isRecruiting ? "Recruiting now" : "Roster closed"}
            </span>

            {team.description && <p className="td__desc">{team.description}</p>}

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
              <h3 className="td__block-title">Captain</h3>
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
              <h3 className="td__block-title">
                Roster <span className="td__block-n">{team.members.length}</span>
              </h3>
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
                <h3 className="td__block-title">
                  Join Requests <span className="td__block-n">{pendingRequests.length}</span>
                </h3>
                {loadingRequests && <p className="drawer__state">Loading requests…</p>}
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
                          <button className="jr__btn jr__btn--accept" onClick={() => handleAccept(r.id)}>✓</button>
                          <button className="jr__btn jr__btn--decline" onClick={() => handleDecline(r.id)}>✕</button>
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </section>
            )}

            <footer className="td__foot">
              {requested ? (
                <p className="td__joined">✓ Request sent! The captain will review it.</p>
              ) : !team.isRecruiting ? (
                <p className="td__closed-note">This team isn't taking new players right now.</p>
              ) : isCaptain ? (
                <p className="td__closed-note">You are the captain of this team.</p>
              ) : requestOpen ? (
                <form className="joinform" onSubmit={submitRequest}>
                  <label className="joinform__label" htmlFor="join-user">
                    Your player id
                  </label>
                  <div className="joinform__row">
                    <input
                      id="join-user"
                      className="joinform__input"
                      inputMode="numeric"
                      placeholder="e.g. 4"
                      value={userId}
                      onChange={(e) => setUserId(e.target.value)}
                      autoFocus
                    />
                    <Button variant="optic" type="submit" disabled={requesting}>
                      {requesting ? "Sending…" : "Send request"}
                    </Button>
                  </div>
                  {requestError && <p className="joinform__error">{requestError}</p>}
                </form>
              ) : (
                <Button variant="solid" onClick={() => setRequestOpen(true)}>
                  Request to join →
                </Button>
              )}
            </footer>
          </>
        )}
      </aside>
    </div>
  );
}
