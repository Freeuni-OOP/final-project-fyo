import { useEffect, useState } from "react";
import { getSports, type SportDto } from "../api/Sports";
import { ApiError, teamsApi } from "../teams/api";
import type { TeamDetails } from "../teams/types";
import { Button } from "../teams/ui";

interface CreateTeamDrawerProps {
  captainUserId: number;
  onClose: () => void;
  onCreated: (team: TeamDetails) => void;
}

export function CreateTeamDrawer({ captainUserId, onClose, onCreated }: CreateTeamDrawerProps) {
  const [sports, setSports] = useState<SportDto[]>([]);
  const [name, setName] = useState("");
  const [sportId, setSportId] = useState("");
  const [region, setRegion] = useState("");
  const [description, setDescription] = useState("");
  const [maxPlayers, setMaxPlayers] = useState("8");
  const [isRecruiting, setIsRecruiting] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

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

    setSubmitting(true);
    setError(null);
    try {
      const created = await teamsApi.create({
        name: name.trim(),
        sportId: sport,
        region: region.trim() || null,
        description: description.trim() || null,
        logoUrl: null,
        maxPlayers: players,
        isRecruiting,
        captainUserId,
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
          You'll be the captain. Post the roster size and players can request a spot.
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
