/* Mirror of backend match DTOs (MatchResponse, MatchParticipantResponse). */

export type MatchFormat = "ONE_VS_ONE" | "TEAM_VS_TEAM";
export type MatchStatus = "UPCOMING" | "COMPLETED" | "CANCELLED";

export interface Sport {
  id: number;
  name: string;
}

export interface MatchParticipant {
  userId: number | null;
  teamId: number | null;
  displayName: string;
  imageUrl: string | null;
}

export interface Match {
  id: number;
  sport: Sport;
  format: MatchFormat;
  home: MatchParticipant;
  away: MatchParticipant;
  location: string | null;
  proposedDatetime: string | null;
  status: MatchStatus;
  createdAt: string;
}
