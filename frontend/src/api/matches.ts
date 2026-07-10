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
  list: (params?: { userId?: number; teamId?: number }) => {
    const q = new URLSearchParams();
    if (params?.userId != null) q.set("userId", String(params.userId));
    if (params?.teamId != null) q.set("teamId", String(params.teamId));
    const qs = q.toString();
    return request<Match[]>(`/api/matches${qs ? `?${qs}` : ""}`);
  },
  mine: (token: string) => request<Match[]>("/api/matches/mine", { token }),
  get: (id: number) => request<Match>(`/api/matches/${id}`),
  cancel: (token: string, id: number) =>
    request<Match>(`/api/matches/${id}/cancel`, {
      method: "POST",
      token,
    }),
};
