import type { TeamDetails, TeamSummary } from "./types";

const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

/** Thrown for any non-2xx response; carries the HTTP status. */
export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = "ApiError";
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
    throw new ApiError(0, "Can't reach the server. Is the backend running?");
  }

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      if (body?.message) message = body.message;
    } catch {
      /* non-JSON error body — keep the default message */
    }
    throw new ApiError(res.status, message);
  }

  return res.status === 204 ? (undefined as T) : ((await res.json()) as T);
}

export const teamsApi = {
  list: () => request<TeamSummary[]>("/api/teams"),
  get: (id: number) => request<TeamDetails>(`/api/teams/${id}`),
  join: (id: number, userId: number) =>
    request<TeamDetails>(`/api/teams/${id}/join`, {
      method: "POST",
      body: JSON.stringify({ userId }),
    }),
};
