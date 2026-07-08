import { useEffect, useState } from "react";
import { ApiError, matchesApi } from "./api";
import type { Match } from "./types";
import { Avatar, Ball, Button } from "../teams/ui";

interface MatchDetailProps {
  matchId: number;
  onClose: () => void;
  onUpdated?: (match: Match) => void;
}

function formatDateTime(iso: string | null): string {
  if (!iso) return "Not set";
  try {
    return new Date(iso).toLocaleString("en-US", {
      weekday: "short",
      day: "numeric",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return iso;
  }
}

function formatLabel(format: Match["format"]): string {
  return format === "ONE_VS_ONE" ? "One vs one" : "Team vs team";
}

export function MatchDetail({ matchId, onClose, onUpdated }: MatchDetailProps) {
  const [match, setMatch] = useState<Match | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [cancelOpen, setCancelOpen] = useState(false);
  const [actingUserId, setActingUserId] = useState("");
  const [cancelling, setCancelling] = useState(false);
  const [cancelError, setCancelError] = useState<string | null>(null);

  useEffect(() => {
    let alive = true;
    setLoading(true);
    setError(null);
    matchesApi
      .get(matchId)
      .then((m) => alive && setMatch(m))
      .catch((e: ApiError) => alive && setError(e.message))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [matchId]);

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

  async function submitCancel(e: React.FormEvent) {
    e.preventDefault();
    const id = Number(actingUserId);
    if (!Number.isInteger(id) || id <= 0) {
      setCancelError("Enter a valid numeric user id.");
      return;
    }
    setCancelling(true);
    setCancelError(null);
    try {
      const updated = await matchesApi.cancel(matchId, id);
      setMatch(updated);
      setCancelOpen(false);
      setActingUserId("");
      onUpdated?.(updated);
    } catch (err) {
      setCancelError((err as ApiError).message);
    } finally {
      setCancelling(false);
    }
  }

  return (
    <div className="drawer" role="dialog" aria-modal="true" aria-label="Match details">
      <div className="drawer__scrim" onClick={onClose} />
      <aside className="drawer__panel">
        <button className="drawer__close" onClick={onClose} aria-label="Close">
          ✕
        </button>

        {loading && <p className="drawer__state">Loading match…</p>}
        {error && !loading && (
          <div className="drawer__state drawer__state--error">
            <p>{error}</p>
            <Button variant="ghost" onClick={onClose}>
              Back to matches
            </Button>
          </div>
        )}

        {match && !loading && (
          <>
            <header className="td__head">
              <div className="td__logo">
                <Ball className="td__logo-ball" />
              </div>
              <div className="td__heading">
                <span className="td__sport">{match.sport.name}</span>
                <h2 className="td__name match-detail__title">
                  {match.home.displayName}
                  <span className="match-row__sep"> vs </span>
                  {match.away.displayName}
                </h2>
                <p className="td__meta">
                  {formatLabel(match.format)} · {match.location ?? "No location"}
                </p>
              </div>
            </header>

            <span
              className={`badge match-status match-status--${match.status.toLowerCase()}`}
            >
              {match.status}
            </span>

            <p className="td__desc">
              Kickoff: <strong>{formatDateTime(match.proposedDatetime)}</strong>
            </p>

            <section className="td__block">
              <h3 className="td__block-title">Home</h3>
              <div className="player">
                <Avatar
                  src={match.home.imageUrl}
                  name={match.home.displayName}
                  size={48}
                />
                <div className="player__id">
                  <span className="player__name">{match.home.displayName}</span>
                  <span className="player__handle">
                    {match.home.userId != null
                      ? `user #${match.home.userId}`
                      : `team #${match.home.teamId}`}
                  </span>
                </div>
                <span className="player__role player__role--captain">Home</span>
              </div>
            </section>

            <section className="td__block">
              <h3 className="td__block-title">Away</h3>
              <div className="player">
                <Avatar
                  src={match.away.imageUrl}
                  name={match.away.displayName}
                  size={48}
                />
                <div className="player__id">
                  <span className="player__name">{match.away.displayName}</span>
                  <span className="player__handle">
                    {match.away.userId != null
                      ? `user #${match.away.userId}`
                      : `team #${match.away.teamId}`}
                  </span>
                </div>
                <span className="player__role">Away</span>
              </div>
            </section>

            <footer className="td__foot">
              {match.status === "CANCELLED" ? (
                <p className="td__closed-note">This match was cancelled.</p>
              ) : match.status === "COMPLETED" ? (
                <p className="td__closed-note">Already played — no cancel.</p>
              ) : cancelOpen ? (
                <form className="joinform" onSubmit={submitCancel}>
                  <label className="joinform__label" htmlFor="cancel-user">
                    Your user id (participant or captain)
                  </label>
                  <div className="joinform__row">
                    <input
                      id="cancel-user"
                      className="joinform__input"
                      inputMode="numeric"
                      placeholder="e.g. 1"
                      value={actingUserId}
                      onChange={(e) => setActingUserId(e.target.value)}
                      autoFocus
                    />
                    <Button variant="optic" type="submit" disabled={cancelling}>
                      {cancelling ? "Cancelling…" : "Confirm"}
                    </Button>
                  </div>
                  {cancelError && (
                    <p className="joinform__error">{cancelError}</p>
                  )}
                </form>
              ) : (
                <Button variant="solid" onClick={() => setCancelOpen(true)}>
                  Cancel match →
                </Button>
              )}
            </footer>
          </>
        )}
      </aside>
    </div>
  );
}
