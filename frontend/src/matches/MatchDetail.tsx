import { useEffect, useRef, useState } from "react";
import { matchesApi, type Match } from "../api/matches";
import { ApiError } from "../api/http";
import { chatMatchPath } from "../chat/routes";
import { Avatar, Ball, Button } from "../teams/ui";

interface MatchDetailProps {
  matchId: number;
  actingUserId: number;
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

export function MatchDetail({ matchId, actingUserId, onClose, onUpdated }: MatchDetailProps) {
  const [match, setMatch] = useState<Match | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [cancelling, setCancelling] = useState(false);
  const [cancelError, setCancelError] = useState<string | null>(null);

  const closeRef = useRef<HTMLButtonElement>(null);
  const panelRef = useRef<HTMLElement>(null);

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
    const previouslyFocused = document.activeElement as HTMLElement | null;
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    closeRef.current?.focus();

    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        onClose();
        return;
      }
      if (e.key !== "Tab" || !panelRef.current) return;

      const focusable = panelRef.current.querySelectorAll<HTMLElement>(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
      );
      if (focusable.length === 0) return;

      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault();
        last.focus();
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault();
        first.focus();
      }
    };

    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = prevOverflow;
      previouslyFocused?.focus();
    };
  }, [onClose]);

  async function handleCancel() {
    setCancelling(true);
    setCancelError(null);
    try {
      const updated = await matchesApi.cancel(matchId, actingUserId);
      setMatch(updated);
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
      <aside className="drawer__panel" ref={panelRef}>
        <button
          ref={closeRef}
          className="drawer__close"
          onClick={onClose}
          aria-label="Close"
        >
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
                </div>
                <span className="player__role">Away</span>
              </div>
            </section>

            <footer className="td__foot">
              {match.status === "UPCOMING" && (
                <p className="td__closed-note">
                  <a href={chatMatchPath(match.id)}>Open match chat →</a>
                </p>
              )}
              {match.status === "CANCELLED" ? (
                <p className="td__closed-note">This match was cancelled.</p>
              ) : match.status === "COMPLETED" ? (
                <p className="td__closed-note">Already played — no cancel.</p>
              ) : (
                <>
                  <Button variant="solid" onClick={() => void handleCancel()} disabled={cancelling}>
                    {cancelling ? "Cancelling…" : "Cancel match →"}
                  </Button>
                  {cancelError && <p className="joinform__error">{cancelError}</p>}
                </>
              )}
            </footer>
          </>
        )}
      </aside>
    </div>
  );
}
