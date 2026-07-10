export type SkillLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";

export interface PlayerSummary {
  id: number;
  username: string;
  name: string;
  surname: string;
  region: string | null;
  imageUrl: string | null;
  sportId: number;
  sportName: string;
  skillLevel: SkillLevel | string;
}

export interface PlayerSearchFilters {
  sportId?: number;
  region?: string;
  skillLevel?: SkillLevel;
  limit?: number;
}