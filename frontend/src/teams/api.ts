import { request } from "../api/http";
import type { CreateTeamPayload, TeamDetails, TeamSummary, JoinRequest } from "./types";

export { ApiError } from "../api/http";

export const teamsApi = {
  list: () => request<TeamSummary[]>("/api/teams"),
  get: (id: number) => request<TeamDetails>(`/api/teams/${id}`),
  create: (body: CreateTeamPayload) =>
    request<TeamDetails>("/api/teams", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  join: (id: number, userId: number) =>
    request<TeamDetails>(`/api/teams/${id}/join`, {
      method: "POST",
      body: JSON.stringify({ userId }),
    }),

  requestToJoin: (teamId: number, token: string) =>
    request<JoinRequest>(`/api/teams/${teamId}/join-requests`, {
      method: "POST",
    }, token),
  getPendingRequests: (teamId: number, token: string) =>
    request<JoinRequest[]>(`/api/teams/${teamId}/join-requests`, undefined, token),
  acceptRequest: (teamId: number, requestId: number, token: string) =>
    request<JoinRequest>(`/api/teams/${teamId}/join-requests/${requestId}/accept`, {
      method: "POST",
    }, token),
  declineRequest: (teamId: number, requestId: number, token: string) =>
    request<JoinRequest>(`/api/teams/${teamId}/join-requests/${requestId}/decline`, {
      method: "POST",
    }, token),
};
