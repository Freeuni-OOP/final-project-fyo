import type { UserAdmin, TeamAdmin, SportAdmin } from "./types";

const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

class AdminApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = "AdminApiError";
  }
}

async function request<T>(path: string, token: string, init?: RequestInit): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${BASE}${path}`, {
      ...init,
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
        ...(init?.headers as Record<string, string> | undefined),
      },
    });
  } catch {
    throw new AdminApiError(0, "Can't reach the server. Is the backend running?");
  }

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      if (body?.message) message = body.message;
      else if (body?.detail) message = body.detail;
    } catch {
      /* non-JSON error body */
    }
    throw new AdminApiError(res.status, message);
  }

  return res.status === 204 ? (undefined as T) : ((await res.json()) as T);
}

export const adminApi = {
  getUsers: (token: string) => request<UserAdmin[]>("/api/admin/users", token),
  archiveUser: (token: string, id: number) =>
    request<UserAdmin>(`/api/admin/users/${id}`, token, { method: "DELETE" }),
  getTeams: (token: string) => request<TeamAdmin[]>("/api/admin/teams", token),
  archiveTeam: (token: string, id: number) =>
    request<TeamAdmin>(`/api/admin/teams/${id}`, token, { method: "DELETE" }),
  getSports: (token: string) => request<SportAdmin[]>("/api/admin/sports", token),
  createSport: (token: string, sportName: string) =>
    request<SportAdmin>("/api/admin/sports", token, {
      method: "POST",
      body: JSON.stringify({ sportName }),
    }),
  deleteSport: (token: string, id: number) =>
    request<void>(`/api/admin/sports/${id}`, token, { method: "DELETE" }),
};

export { AdminApiError };
