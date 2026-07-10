import { useEffect, useState } from "react";
import { getSports, type SportDto } from "../api/Sports";
import { usersApi } from "../api/users";
import { useAuth } from "../hooks/useAuth";
import { ApiError, teamsApi } from "../teams/api";
import type { TeamDetails, UserSummary } from "../teams/types";
import { Avatar, Button } from "../teams/ui";

interface CreateTeamDrawerProps {
  captainUserId: number;
  onClose: () => void;
  onCreated: (team: TeamDetails) => void;
}

/** Below this the search would match most of the table; the backend returns [] anyway. */
const MIN_SEARCH_LENGTH = 2;

function fullName(user: UserSummary): string {
  return `${user.name} ${user.surname}`.trim() || user.username;
}

export function CreateTeamDrawer({ captainUserId, onClose, onCreated }: CreateTeamDrawerProps) {
  const { getIdToken } = useAuth();
  const [sports, setSports] = useState<SportDto[]>([]);
  const [name, setName] = useState("");
  const [sportId, setSportId] = useState("");
  const [region, setRegion] = useState("");
  const [description, setDescription] = useState("");
  const [maxPlayers, setMaxPlayers] = useState("8");
  const [isRecruiting, setIsRecruiting] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [members, setMembers] = useState<UserSummary[]>([]);
  const [term, setTerm] = useState("");
  const [results, setResults] = useState<UserSummary[]>([]);
  const [searching, setSearching] = useState(false);

  const capacity = Number(maxPlayers);
  // The captain always takes one seat, so that's how many others can fit.
  const otherSeats = Number.isInteger(capacity) && capacity > 0 ? capacity - 1 : 0;
  const seatsLeft = Math.max(0, otherSeats - members.length);

  useEffect(() => {
    getSports().then(setSports).catch(() => setError("Could not load the sport list."));
  }, []);

  // Close on Escape; lock body scroll while the drawer is open.
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => e.key === "Escape" && onClose();
    document.addEventListener("keydown", onKey);
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = previousOverflow;
    };
  }, [onClose]);

  useEffect(() => {
    const trimmed = term.trim();
    if (trimmed.length < MIN_SEARCH_LENGTH) {
      setResults([]);
      setSearching(false);
      return;
    }

    setSearching(true);
    let alive = true;
    // Debounced so a fast typist doesn't fire a request per keystroke.
    const timer = setTimeout(() => {
      usersApi
        .search(trimmed)
        .then((found) => alive && setResults(found))
        .catch(() => alive && setResults([]))
        .finally(() => alive && setSearching(false));
    }, 250);

    return () => {
      alive = false;
      clearTimeout(timer);
    };
  }, [term]);

  const picked = new Set(members.map((m) => m.id));
  const selectable = results.filter((u) => u.id !== captainUserId && !picked.has(u.id));

  function addMember(user: UserSummary) {
    setMembers((prev) => [...prev, user]);
    setTerm("");
    setResults([]);
    setError(null);
  }

  function removeMember(userId: number) {
    setMembers((prev) => prev.filter((m) => m.id !== userId));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();

    const sport = Number(sportId);
    const players = Number(maxPlayers);
    if (!Number.isInteger(sport) || sport <= 0) {
      setError("Pick a sport.");
      return;
    }
    if (!Number.isInteger(players) || players < 1 || players > 100) {
      setError("Roster size must be between 1 and 100.");
      return;
    }
    if (members.length > players - 1) {
      setError(
        `A roster of ${players} seats only ${players - 1} players besides you. ` +
          `Remove ${members.length - (players - 1)} or raise the roster size.`
      );
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      const token = await getIdToken();
      if (!token) {
        setError("Your session expired. Sign in again.");
        return;
      }
      const created = await teamsApi.create(token, {
        name: name.trim(),
        sportId: sport,
        region: region.trim() || null,
        description: description.trim() || null,
        logoUrl: null,
        maxPlayers: players,
        isRecruiting,
        memberUserIds: members.map((m) => m.id),
      });
      onCreated(created);
      onClose();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not create the team.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="drawer" role="dialog" aria-modal="true" aria-label="Create a team">
      <div className="drawer__scrim" onClick={onClose} />
      <aside className="drawer__panel">
        <button className="drawer__close" onClick={onClose} aria-label="Close">
          ✕
        </button>

        <p className="eyebrow">Captain's tools</p>
        <h2 className="td__name">Start a team</h2>
        <p className="td__desc">
          You'll be the captain. Add players you already know, and post the roster size so
          the rest can request a spot.
        </p>

        <form className="form" onSubmit={handleSubmit}>
          <div className="form__field">
            <label className="form__label" htmlFor="team-name">
              Team name
            </label>
            <input
              id="team-name"
              className="form__input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              maxLength={255}
              required
              autoFocus
            />
          </div>

          <div className="form__row">
            <div className="form__field">
              <label className="form__label" htmlFor="team-sport">
                Sport
              </label>
              <select
                id="team-sport"
                className="form__select"
                value={sportId}
                onChange={(e) => setSportId(e.target.value)}
                required
              >
                <option value="" disabled>
                  Choose one
                </option>
                {sports.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.sportName}
                  </option>
                ))}
              </select>
            </div>

            <div className="form__field">
              <label className="form__label" htmlFor="team-max">
                Roster size
              </label>
              <input
                id="team-max"
                className="form__input"
                type="number"
                min={1}
                max={100}
                value={maxPlayers}
                onChange={(e) => setMaxPlayers(e.target.value)}
                required
              />
            </div>
          </div>

          <div className="form__field">
            <label className="form__label" htmlFor="team-region">
              Region
            </label>
            <input
              id="team-region"
              className="form__input"
              value={region}
              onChange={(e) => setRegion(e.target.value)}
              maxLength={255}
              placeholder="e.g. Vake"
            />
          </div>

          <div className="form__field">
            <label className="form__label" htmlFor="team-members">
              Players
              <span className="form__hint">
                {seatsLeft} of {otherSeats} spots left
              </span>
            </label>

            {members.length > 0 && (
              <ul className="picks">
                {members.map((m) => (
                  <li className="pick" key={m.id}>
                    <Avatar src={m.imageUrl} name={fullName(m)} size={28} />
                    <span className="pick__name">{fullName(m)}</span>
                    <span className="pick__handle">@{m.username}</span>
                    <button
                      type="button"
                      className="pick__remove"
                      onClick={() => removeMember(m.id)}
                      aria-label={`Remove ${m.username}`}
                    >
                      ✕
                    </button>
                  </li>
                ))}
              </ul>
            )}

            <input
              id="team-members"
              className="form__input"
              value={term}
              onChange={(e) => setTerm(e.target.value)}
              placeholder={
                seatsLeft > 0 ? "Search by name or username" : "Every spot is taken"
              }
              autoComplete="off"
              disabled={seatsLeft === 0}
            />

            {searching && <p className="picker__note">Searching…</p>}

            {!searching && term.trim().length >= MIN_SEARCH_LENGTH && selectable.length === 0 && (
              <p className="picker__note">No other players match "{term.trim()}".</p>
            )}

            {selectable.length > 0 && (
              <ul className="picker">
                {selectable.map((u) => (
                  <li key={u.id}>
                    <button
                      type="button"
                      className="picker__opt"
                      onClick={() => addMember(u)}
                    >
                      <Avatar src={u.imageUrl} name={fullName(u)} size={32} />
                      <span className="picker__id">
                        <span className="picker__name">{fullName(u)}</span>
                        <span className="picker__handle">
                          @{u.username}
                          {u.region ? ` · ${u.region}` : ""}
                        </span>
                      </span>
                      <span className="picker__add">Add</span>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="form__field">
            <label className="form__label" htmlFor="team-desc">
              Description
            </label>
            <textarea
              id="team-desc"
              className="form__textarea"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="When you play, what level, who you're looking for."
            />
          </div>

          <label className="form__check">
            <input
              type="checkbox"
              checked={isRecruiting}
              onChange={(e) => setIsRecruiting(e.target.checked)}
            />
            Open for new players
          </label>

          {error && (
            <p className="form__error" role="alert">
              {error}
            </p>
          )}

          <div className="form__actions">
            <Button variant="solid" type="submit" disabled={submitting}>
              {submitting ? "Creating…" : "Create team"}
            </Button>
            <Button variant="ghost" type="button" onClick={onClose}>
              Cancel
            </Button>
          </div>
        </form>
      </aside>
    </div>
  );
}
