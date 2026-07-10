import type { UserAdmin, TeamAdmin, SportAdmin } from "./types";

const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

class AdminApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = "AdminApiError";
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${BASE}${path}`, {
      headers: { "Content-Type": "application/json" },
      ...init,
    });
  } catch {
    throw new AdminApiError(0, "Can't reach the server. Is the backend running?");
  }

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      if (body?.message) message = body.message;
    } catch {
      /* non-JSON error body */
    }
    throw new AdminApiError(res.status, message);
  }

  return res.status === 204 ? (undefined as T) : ((await res.json()) as T);
}

const ADMIN_ID = 1; // placeholder until real auth admin check

export const adminApi = {
  getUsers: () =>
    request<UserAdmin[]>(`/api/admin/users?adminUserId=${ADMIN_ID}`),
  archiveUser: (id: number) =>
    request<UserAdmin>(`/api/admin/users/${id}?adminUserId=${ADMIN_ID}`, {
      method: "DELETE",
    }),
  getTeams: () =>
    request<TeamAdmin[]>(`/api/admin/teams?adminUserId=${ADMIN_ID}`),
  archiveTeam: (id: number) =>
    request<TeamAdmin>(`/api/admin/teams/${id}?adminUserId=${ADMIN_ID}`, {
      method: "DELETE",
    }),
  getSports: () =>
    request<SportAdmin[]>(`/api/admin/sports?adminUserId=${ADMIN_ID}`),
  createSport: (sportName: string) =>
    request<SportAdmin>(`/api/admin/sports?adminUserId=${ADMIN_ID}`, {
      method: "POST",
      body: JSON.stringify({ sportName }),
    }),
  deleteSport: (id: number) =>
    request<void>(`/api/admin/sports/${id}?adminUserId=${ADMIN_ID}`, {
      method: "DELETE",
    }),
};

export { AdminApiError };