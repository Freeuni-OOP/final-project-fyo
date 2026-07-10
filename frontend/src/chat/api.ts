import type { ChatMessage, Conversation } from "./types";

const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = "ApiError";
  }
}

/** Identity travels only in the Bearer token; the backend resolves the
 *  caller from it, so no endpoint takes a user id anymore. */
async function request<T>(
  path: string,
  token: string,
  init?: RequestInit
): Promise<T> {
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
    throw new ApiError(0, "Can't reach the server. Is the backend running?");
  }

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      if (body?.message) message = body.message;
      else if (body?.detail) message = body.detail;
    } catch {
      /* keep default */
    }
    throw new ApiError(res.status, message);
  }

  return res.status === 204 ? (undefined as T) : ((await res.json()) as T);
}

export const chatApi = {
  conversations: (token: string) =>
    request<Conversation[]>("/api/conversations", token),

  createDirect: (token: string, otherUserId: number) =>
    request<Conversation>("/api/conversations/direct", token, {
      method: "POST",
      body: JSON.stringify({ otherUserId }),
    }),

  createTeam: (token: string, teamId: number) =>
    request<Conversation>(`/api/conversations/team/${teamId}`, token, {
      method: "POST",
    }),

  byMatch: (token: string, matchId: number) =>
    request<Conversation>(`/api/conversations/by-match/${matchId}`, token),

  messages: (
    token: string,
    conversationId: number,
    options?: { before?: number; limit?: number }
  ) => {
    const params = new URLSearchParams();
    if (options?.before != null) params.set("before", String(options.before));
    if (options?.limit != null) params.set("limit", String(options.limit));
    const query = params.toString();
    return request<ChatMessage[]>(
      `/api/conversations/${conversationId}/messages${query ? `?${query}` : ""}`,
      token
    );
  },

  send: (token: string, conversationId: number, body: string) =>
    request<ChatMessage>(`/api/conversations/${conversationId}/messages`, token, {
      method: "POST",
      body: JSON.stringify({ body }),
    }),
};

export function socketUrl(): string {
  const url = new URL(BASE);
  url.protocol = url.protocol === "https:" ? "wss:" : "ws:";
  url.pathname = "/ws";
  url.search = "";
  return url.toString();
}
