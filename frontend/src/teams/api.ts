import { request } from "../api/http";
import type {
  CreateTeamPayload,
  JoinRequest,
  MyJoinRequest,
  MyTeam,
  TeamDetails,
  TeamSummary,
} from "./types";

export { ApiError } from "../api/http";

export const teamsApi = {
  list: () => request<TeamSummary[]>("/api/teams"),
  get: (id: number) => request<TeamDetails>(`/api/teams/${id}`),
  myTeams: (userId: number) => request<MyTeam[]>(`/api/teams/mine?userId=${userId}`),
  myJoinRequests: (userId: number) =>
    request<MyJoinRequest[]>(`/api/teams/my-requests?userId=${userId}`),
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

  requestToJoin: (teamId: number, userId: number) =>
    request<JoinRequest>(`/api/teams/${teamId}/join-requests?userId=${userId}`, {
      method: "POST",
    }),
  getPendingRequests: (teamId: number) =>
    request<JoinRequest[]>(`/api/teams/${teamId}/join-requests`),
  acceptRequest: (teamId: number, requestId: number, captainUserId: number) =>
    request<JoinRequest>(`/api/teams/${teamId}/join-requests/${requestId}/accept?captainUserId=${captainUserId}`, {
      method: "POST",
    }),
  declineRequest: (teamId: number, requestId: number, captainUserId: number) =>
    request<JoinRequest>(`/api/teams/${teamId}/join-requests/${requestId}/decline?captainUserId=${captainUserId}`, {
      method: "POST",
    }),
};
