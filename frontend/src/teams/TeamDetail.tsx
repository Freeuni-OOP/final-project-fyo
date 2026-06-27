import { useEffect, useState } from "react";
import { ApiError, teamsApi } from "./api";
import type { TeamDetails } from "./types";
import { Avatar, Ball, Button } from "./ui";

interface TeamDetailProps {
  teamId: number;
  onClose: () => void;
  /** Bubble an updated roster up so the list view stays in sync after a join. */
  onJoined?: (team: TeamDetails) => void;
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

export function TeamDetail({ teamId, onClose, onJoined }: TeamDetailProps) {
  const [team, setTeam] = useState<TeamDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [joinOpen, setJoinOpen] = useState(false);
  const [userId, setUserId] = useState("");
  const [joining, setJoining] = useState(false);
  const [joinError, setJoinError] = useState<string | null>(null);
  const [joined, setJoined] = useState(false);

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

  async function submitJoin(e: React.FormEvent) {
    e.preventDefault();
    const id = Number(userId);
    if (!Number.isInteger(id) || id <= 0) {
      setJoinError("Enter a valid numeric player id.");
      return;
    }
    setJoining(true);
    setJoinError(null);
    try {
      const updated = await teamsApi.join(teamId, id);
      setTeam(updated);
      setJoined(true);
      setJoinOpen(false);
      setUserId("");
      onJoined?.(updated);
    } catch (err) {
      setJoinError((err as ApiError).message);
    } finally {
      setJoining(false);
    }
  }

  const filled = team ? team.maxPlayers - team.openSpots : 0;
  const pct = team ? Math.round((filled / team.maxPlayers) * 100) : 0;

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

            <footer className="td__foot">
              {joined ? (
                <p className="td__joined">✓ You're on the roster. See you on the court.</p>
              ) : !team.isRecruiting ? (
                <p className="td__closed-note">
                  This team isn't taking new players right now.
                </p>
              ) : joinOpen ? (
                <form className="joinform" onSubmit={submitJoin}>
                  <label className="joinform__label" htmlFor="join-user">
                    Player id
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
                    <Button variant="optic" type="submit" disabled={joining}>
                      {joining ? "Joining…" : "Confirm"}
                    </Button>
                  </div>
                  {joinError && <p className="joinform__error">{joinError}</p>}
                </form>
              ) : (
                <Button variant="solid" onClick={() => setJoinOpen(true)}>
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
