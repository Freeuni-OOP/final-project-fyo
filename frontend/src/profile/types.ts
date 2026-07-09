export type SkillLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
export type Sex = "MALE" | "FEMALE" | "OTHER";

export interface ProfileSport {
  sportId: number;
  sportName: string;
  skillLevel: SkillLevel | string;
}

export interface MatchHistoryItem {
  matchId: number;
  sportName: string;
  format: string;
  status: string;
  location: string | null;
  proposedDatetime: string | null;
  opponentUserId: number | null;
  opponentUsername: string | null;
  homeTeamId: number | null;
  homeTeamName: string | null;
  awayTeamId: number | null;
  awayTeamName: string | null;
  homeScore: number | null;
  awayScore: number | null;
  winner: string | null;
}

export interface ReviewItem {
  id: number;
  matchId: number;
  reviewerUserId: number;
  reviewerUsername: string;
  score: number;
  comment: string | null;
  createdAt: string | null;
}

export interface Profile {
  id: number;
  username: string;
  name: string;
  surname: string;
  age: number | null;
  sex: string | null;
  region: string | null;
  imageUrl: string | null;
  email: string | null;
  ratingAverage: number | null;
  sports: ProfileSport[];
  matchHistory: MatchHistoryItem[];
  reviews: ReviewItem[];
}

export interface ProfileSportUpdate {
  sportId: number;
  skillLevel: string;
}

export interface UpdateProfileRequest {
  name: string;
  surname: string;
  username: string;
  age: number;
  sex: string;
  region: string | null;
  imageUrl: string | null;
  sports: ProfileSportUpdate[];
}
