import type { Match } from "./types";

const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

/** Same idea as teams ApiError — keep status for UI handling. */
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
      /* non-JSON body */
    }
    throw new ApiError(res.status, message);
  }

  return res.status === 204 ? (undefined as T) : ((await res.json()) as T);
}

export const matchesApi = {
  list: (params?: { userId?: number; teamId?: number }) => {
    const q = new URLSearchParams();
    if (params?.userId != null) q.set("userId", String(params.userId));
    if (params?.teamId != null) q.set("teamId", String(params.teamId));
    const qs = q.toString();
    return request<Match[]>(`/api/matches${qs ? `?${qs}` : ""}`);
  },
  get: (id: number) => request<Match>(`/api/matches/${id}`),
  // Backend MatchController takes actingUserId as @RequestParam on POST, not a JSON body.
  cancel: (id: number, actingUserId: number) =>
    request<Match>(`/api/matches/${id}/cancel?actingUserId=${actingUserId}`, {
      method: "POST",
    }),
};
