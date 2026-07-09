import type { Profile, UpdateProfileRequest } from "./types";

const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

export class ProfileApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = "ProfileApiError";
  }
}

async function request<T>(
  path: string,
  init?: RequestInit & { token?: string }
): Promise<T> {
  const { token, headers, ...rest } = init ?? {};
  const mergedHeaders: Record<string, string> = {
    "Content-Type": "application/json",
    ...(headers as Record<string, string> | undefined),
  };
  if (token) {
    mergedHeaders.Authorization = `Bearer ${token}`;
  }

  let res: Response;
  try {
    res = await fetch(`${BASE}${path}`, {
      ...rest,
      headers: mergedHeaders,
    });
  } catch {
    throw new ProfileApiError(0, "Can't reach the server. Is the backend running?");
  }

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      if (body?.message) message = body.message;
    } catch {
      /* keep default */
    }
    throw new ProfileApiError(res.status, message);
  }

  return res.status === 204 ? (undefined as T) : ((await res.json()) as T);
}

export const profileApi = {
  getPublic: (userId: number) => request<Profile>(`/api/profiles/${userId}`),

  getMe: (token: string) =>
    request<Profile>("/api/profiles/me", { token }),

  updateMe: (token: string, body: UpdateProfileRequest) =>
    request<Profile>("/api/profiles/me", {
      method: "PUT",
      token,
      body: JSON.stringify(body),
    }),
};
