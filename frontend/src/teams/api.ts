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
  myTeams: (token: string) =>
    request<MyTeam[]>("/api/teams/mine", { token }),
  myJoinRequests: (token: string) =>
    request<MyJoinRequest[]>("/api/teams/my-requests", { token }),
  create: (token: string, body: CreateTeamPayload) =>
    request<TeamDetails>("/api/teams", {
      method: "POST",
      body: JSON.stringify(body),
      token,
    }),
  join: (token: string, id: number) =>
    request<TeamDetails>(`/api/teams/${id}/join`, {
      method: "POST",
      token,
    }),

  requestToJoin: (token: string, teamId: number) =>
    request<JoinRequest>(`/api/teams/${teamId}/join-requests`, {
      method: "POST",
      token,
    }),
  getPendingRequests: (token: string, teamId: number) =>
    request<JoinRequest[]>(`/api/teams/${teamId}/join-requests`, { token }),
  acceptRequest: (token: string, teamId: number, requestId: number) =>
    request<JoinRequest>(`/api/teams/${teamId}/join-requests/${requestId}/accept`, {
      method: "POST",
      token,
    }),
  declineRequest: (token: string, teamId: number, requestId: number) =>
    request<JoinRequest>(`/api/teams/${teamId}/join-requests/${requestId}/decline`, {
      method: "POST",
      token,
    }),
};

