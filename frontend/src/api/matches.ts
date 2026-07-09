import { request } from "./http";

/* TypeScript mirror of `com.fyo.match.dto`. Keep field names in sync. */

export type MatchStatus = "UPCOMING" | "COMPLETED" | "CANCELLED";
export type MatchFormat = "ONE_VS_ONE" | "TEAM_VS_TEAM";

export interface MatchSport {
  id: number;
  name: string;
}

/** One side of a match: `userId` for ONE_VS_ONE, `teamId` for TEAM_VS_TEAM.
 *  Render it the same way either way — never branch on the format. */
export interface MatchParticipant {
  userId: number | null;
  teamId: number | null;
  displayName: string;
  imageUrl: string | null;
}

export interface Match {
  id: number;
  sport: MatchSport;
  format: MatchFormat;
  home: MatchParticipant;
  away: MatchParticipant;
  location: string | null;
  proposedDatetime: string | null;
  status: MatchStatus;
  createdAt: string;
}

export const matchesApi = {
  listForUser: (userId: number) => request<Match[]>(`/api/matches?userId=${userId}`),
  get: (id: number) => request<Match>(`/api/matches/${id}`),
  cancel: (id: number, actingUserId: number) =>
    request<Match>(`/api/matches/${id}/cancel?actingUserId=${actingUserId}`, {
      method: "POST",
    }),
};
