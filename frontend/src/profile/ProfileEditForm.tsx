import { useMemo, useState } from "react";
import type { SportDto } from "../api/Sports";
import type { Profile, SkillLevel, Sex, UpdateProfileRequest } from "./types";

const SKILL_LEVELS: SkillLevel[] = ["BEGINNER", "INTERMEDIATE", "ADVANCED"];

type Fields = {
  name: string;
  surname: string;
  username: string;
  age: string;
  sex: Sex | "";
  region: string;
  imageUrl: string;
};

function toFields(profile: Profile): Fields {
  return {
    name: profile.name ?? "",
    surname: profile.surname ?? "",
    username: profile.username ?? "",
    age: profile.age != null ? String(profile.age) : "",
    sex: (profile.sex as Sex | null) ?? "",
    region: profile.region ?? "",
    imageUrl: profile.imageUrl ?? "",
  };
}

function toSelectedSports(profile: Profile): Record<number, SkillLevel> {
  const next: Record<number, SkillLevel> = {};
  for (const sport of profile.sports) {
    const level = String(sport.skillLevel).toUpperCase();
    if (level === "BEGINNER" || level === "INTERMEDIATE" || level === "ADVANCED") {
      next[sport.sportId] = level;
    } else {
      next[sport.sportId] = "BEGINNER";
    }
  }
  return next;
}

export function ProfileEditForm({
  profile,
  sports,
  submitting,
  error,
  onCancel,
  onSubmit,
}: {
  profile: Profile;
  sports: SportDto[];
  submitting: boolean;
  error: string | null;
  onCancel: () => void;
  onSubmit: (payload: UpdateProfileRequest) => void;
}) {
  const [fields, setFields] = useState<Fields>(() => toFields(profile));
  const [selectedSports, setSelectedSports] = useState<Record<number, SkillLevel>>(
    () => toSelectedSports(profile)
  );
  const [localError, setLocalError] = useState<string | null>(null);

  const sportCount = useMemo(
    () => Object.keys(selectedSports).length,
    [selectedSports]
  );

  function setField<K extends keyof Fields>(key: K, value: Fields[K]) {
    setFields((prev) => ({ ...prev, [key]: value }));
  }

  function toggleSport(id: number) {
    setSelectedSports((prev) => {
      if (id in prev) {
        const next = { ...prev };
        delete next[id];
        return next;
      }
      return { ...prev, [id]: "BEGINNER" };
    });
  }

  function setSkill(id: number, level: SkillLevel) {
    setSelectedSports((prev) => ({ ...prev, [id]: level }));
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!fields.name.trim() || !fields.surname.trim() || !fields.username.trim()) {
      setLocalError("Name, surname and username are required.");
      return;
    }
    const age = Number(fields.age);
    if (!fields.age || Number.isNaN(age) || age < 10 || age > 100) {
      setLocalError("Age must be between 10 and 100.");
      return;
    }
    if (!fields.sex) {
      setLocalError("Please select your sex.");
      return;
    }
    if (sportCount === 0) {
      setLocalError("Pick at least one sport.");
      return;
    }

    setLocalError(null);
    onSubmit({
      name: fields.name.trim(),
      surname: fields.surname.trim(),
      username: fields.username.trim(),
      age,
      sex: fields.sex,
      region: fields.region.trim() || null,
      imageUrl: fields.imageUrl.trim() || null,
      sports: Object.entries(selectedSports).map(([sportId, skillLevel]) => ({
        sportId: Number(sportId),
        skillLevel,
      })),
    });
  }

  return (
    <form className="pf-edit" onSubmit={handleSubmit}>
      {profile.email && (
        <p className="pf-edit__email">
          Email (read-only): <strong>{profile.email}</strong>
        </p>
      )}

      <div className="pf-edit__grid">
        <div className="pf-field">
          <label htmlFor="pf-name">First name</label>
          <input
            id="pf-name"
            value={fields.name}
            onChange={(e) => setField("name", e.target.value)}
          />
        </div>
        <div className="pf-field">
          <label htmlFor="pf-surname">Last name</label>
          <input
            id="pf-surname"
            value={fields.surname}
            onChange={(e) => setField("surname", e.target.value)}
          />
        </div>
        <div className="pf-field pf-field--full">
          <label htmlFor="pf-username">Username</label>
          <input
            id="pf-username"
            value={fields.username}
            onChange={(e) => setField("username", e.target.value)}
          />
        </div>
        <div className="pf-field">
          <label htmlFor="pf-age">Age</label>
          <input
            id="pf-age"
            type="number"
            min={10}
            max={100}
            value={fields.age}
            onChange={(e) => setField("age", e.target.value)}
          />
        </div>
        <div className="pf-field">
          <label htmlFor="pf-sex">Sex</label>
          <select
            id="pf-sex"
            value={fields.sex}
            onChange={(e) => setField("sex", e.target.value as Sex | "")}
          >
            <option value="" disabled>
              Select…
            </option>
            <option value="MALE">Male</option>
            <option value="FEMALE">Female</option>
            <option value="OTHER">Other</option>
          </select>
        </div>
        <div className="pf-field pf-field--full">
          <label htmlFor="pf-region">Region</label>
          <input
            id="pf-region"
            value={fields.region}
            onChange={(e) => setField("region", e.target.value)}
          />
        </div>
        <div className="pf-field pf-field--full">
          <label htmlFor="pf-image">Profile image URL</label>
          <input
            id="pf-image"
            type="url"
            value={fields.imageUrl}
            onChange={(e) => setField("imageUrl", e.target.value)}
          />
        </div>
      </div>

      <h3 className="pf-edit__subtitle">Sports</h3>
      <ul className="pf-edit__sports">
        {sports.map((sport) => {
          const selected = sport.id in selectedSports;
          const skill = selectedSports[sport.id];
          return (
            <li
              key={sport.id}
              className={`pf-edit__sport ${selected ? "pf-edit__sport--on" : ""}`}
            >
              <button
                type="button"
                className="pf-edit__sport-row"
                onClick={() => toggleSport(sport.id)}
                aria-pressed={selected}
              >
                <span>{sport.sportName}</span>
                <span>{selected ? "✕" : "+"}</span>
              </button>
              {selected && (
                <div className="pf-edit__levels">
                  {SKILL_LEVELS.map((level) => (
                    <button
                      key={level}
                      type="button"
                      className={`pf-edit__level ${skill === level ? "pf-edit__level--on" : ""}`}
                      onClick={() => setSkill(sport.id, level)}
                    >
                      {level}
                    </button>
                  ))}
                </div>
              )}
            </li>
          );
        })}
      </ul>

      {(localError || error) && (
        <p className="pf-error" role="alert">
          {localError || error}
        </p>
      )}

      <div className="pf-edit__actions">
        <button type="button" className="btn btn--ghost" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
        <button type="submit" className="btn btn--solid" disabled={submitting || sportCount === 0}>
          {submitting ? "Saving…" : "Save profile"}
        </button>
      </div>
    </form>
  );
}
