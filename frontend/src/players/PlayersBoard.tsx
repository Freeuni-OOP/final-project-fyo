import { useMemo, useState } from "react";
import type { PlayerSummary, SkillLevel } from "./types";
import { useReveal } from "../teams/useReveal";
import { Avatar, Button } from "../teams/ui";
import "./players.css";

const SKILL_LEVELS: SkillLevel[] = ["BEGINNER", "INTERMEDIATE", "ADVANCED"];

const SKILL_LABEL: Record<SkillLevel, string> = {
  BEGINNER: "Beginner",
  INTERMEDIATE: "Intermediate",
  ADVANCED: "Advanced",
};

interface PlayerCardData {
  id: number;
  username: string;
  fullName: string;
  region: string | null;
  imageUrl: string | null;
  sports: { sportName: string; skillLevel: SkillLevel | string }[];
}

function groupByPlayer(rows: PlayerSummary[]): PlayerCardData[] {
  const byId = new Map<number, PlayerCardData>();
  for (const row of rows) {
    const existing = byId.get(row.id);
    const entry = { sportName: row.sportName, skillLevel: row.skillLevel };
    if (existing) {
      existing.sports.push(entry);
    } else {
      byId.set(row.id, {
        id: row.id,
        username: row.username,
        fullName: `${row.name} ${row.surname}`.trim() || row.username,
        region: row.region,
        imageUrl: row.imageUrl,
        sports: [entry],
      });
    }
  }
  return Array.from(byId.values()).sort((a, b) => a.username.localeCompare(b.username));
}

interface PlayersBoardProps {
  rows: PlayerSummary[];
  loading: boolean;
  error: string | null;
  onRetry: () => void;
  basePath?: string;
}

export function PlayersBoard({ rows, loading, error, onRetry, basePath = "#/profile" }: PlayersBoardProps) {
  const [sport, setSport] = useState<string>("ALL");
  const [region, setRegion] = useState<string>("ALL");
  const [skill, setSkill] = useState<string>("ALL");

  const players = useMemo(() => groupByPlayer(rows), [rows]);

  const sports = useMemo(
    () => Array.from(new Set(rows.map((r) => r.sportName))).sort(),
    [rows]
  );
  const regions = useMemo(
    () =>
      Array.from(new Set(rows.map((r) => r.region).filter((r): r is string => !!r))).sort(),
    [rows]
  );

  const visible = useMemo(
    () =>
      players.filter((p) => {
        if (region !== "ALL" && p.region !== region) return false;
        return p.sports.some(
          (s) =>
            (sport === "ALL" || s.sportName === sport) &&
            (skill === "ALL" || s.skillLevel === skill)
        );
      }),
    [players, sport, region, skill]
  );

  useReveal([visible.length, loading]);

  return (
    <>
      <div className="teams__controls" data-reveal>
        <div className="chips" role="group" aria-label="Filter by sport">
          <button
            className={`chip ${sport === "ALL" ? "chip--on" : ""}`}
            onClick={() => setSport("ALL")}
          >
            All sports
          </button>
          {sports.map((s) => (
            <button
              key={s}
              className={`chip ${sport === s ? "chip--on" : ""}`}
              onClick={() => setSport(s)}
            >
              {s}
            </button>
          ))}
        </div>

        {regions.length > 0 && (
          <div className="chips" role="group" aria-label="Filter by region">
            <button
              className={`chip ${region === "ALL" ? "chip--on" : ""}`}
              onClick={() => setRegion("ALL")}
            >
              All regions
            </button>
            {regions.map((r) => (
              <button
                key={r}
                className={`chip ${region === r ? "chip--on" : ""}`}
                onClick={() => setRegion(r)}
              >
                {r}
              </button>
            ))}
          </div>
        )}

        <div className="chips" role="group" aria-label="Filter by skill level">
          <button
            className={`chip ${skill === "ALL" ? "chip--on" : ""}`}
            onClick={() => setSkill("ALL")}
          >
            All levels
          </button>
          {SKILL_LEVELS.map((lvl) => (
            <button
              key={lvl}
              className={`chip ${skill === lvl ? "chip--on" : ""}`}
              onClick={() => setSkill(lvl)}
            >
              {SKILL_LABEL[lvl]}
            </button>
          ))}
        </div>
      </div>

      {loading && <p className="teams__state">Loading players…</p>}

      {error && !loading && (
        <div className="teams__state teams__state--error">
          <p>{error}</p>
          <Button variant="ghost" onClick={onRetry}>
            Try again
          </Button>
        </div>
      )}

      {!loading && !error && visible.length === 0 && (
        <p className="teams__state">No players match that filter yet.</p>
      )}

      {!loading && !error && visible.length > 0 && (
        <ul className="playercards">
          {visible.map((p, i) => (
            <PlayerCard
              key={p.id}
              player={p}
              index={i}
              href={`${basePath}/${p.id}`}
              highlightSport={sport === "ALL" ? undefined : sport}
            />
          ))}
        </ul>
      )}
    </>
  );
}

interface PlayerCardProps {
  player: PlayerCardData;
  index: number;
  href: string;
  highlightSport?: string;
}

function PlayerCard({ player, index, href, highlightSport }: PlayerCardProps) {
  const shown = highlightSport
    ? player.sports.filter((s) => s.sportName === highlightSport)
    : player.sports;

  return (
    <li className="playercard-item">
      <a
        className="playercard"
        href={href}
        data-reveal
        style={{ transitionDelay: `${Math.min(index, 8) * 40}ms` }}
      >
        <div className="playercard__top">
          <Avatar src={player.imageUrl} name={player.fullName} size={56} />
          <div className="playercard__id">
            <span className="playercard__name">{player.fullName}</span>
            <span className="playercard__handle">@{player.username}</span>
          </div>
        </div>

        <div className="playercard__meta">
          <span className="playercard__region">{player.region ?? "Region not set"}</span>
        </div>

        <ul className="playercard__sports">
          {shown.map((s) => (
            <li key={s.sportName} className="playercard__sport">
              <span>{s.sportName}</span>
              <em className={`skillpill skillpill--${String(s.skillLevel).toLowerCase()}`}>
                {SKILL_LABEL[s.skillLevel as SkillLevel] ?? s.skillLevel}
              </em>
            </li>
          ))}
        </ul>

        <span className="playercard__go" aria-hidden="true">
          View profile →
        </span>
      </a>
    </li>
  );
}