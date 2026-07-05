import type { ChatMessage, Conversation } from "./types";

const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

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
      /* keep default */
    }
    throw new ApiError(res.status, message);
  }

  return res.status === 204 ? (undefined as T) : ((await res.json()) as T);
}

export const chatApi = {
  conversations: (userId: number) =>
    request<Conversation[]>(`/api/conversations?userId=${userId}`),

  createDirect: (userAId: number, userBId: number) =>
    request<Conversation>("/api/conversations/direct", {
      method: "POST",
      body: JSON.stringify({ userAId, userBId }),
    }),

  messages: (conversationId: number, userId: number) =>
    request<ChatMessage[]>(
      `/api/conversations/${conversationId}/messages?userId=${userId}`
    ),

  send: (conversationId: number, senderUserId: number, body: string) =>
    request<ChatMessage>(`/api/conversations/${conversationId}/messages`, {
      method: "POST",
      body: JSON.stringify({ senderUserId, body }),
    }),
};

export function socketUrl(): string {
  const url = new URL(BASE);
  url.protocol = url.protocol === "https:" ? "wss:" : "ws:";
  url.pathname = "/ws";
  url.search = "";
  return url.toString();
}
