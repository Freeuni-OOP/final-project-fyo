import type { FriendRequest, FriendStatus, FriendSummary } from "./types";

const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

export class FriendsApiError extends Error {
  constructor(
    public status: number,
    message: string
  ) {
    super(message);
    this.name = "FriendsApiError";
  }
}

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
    throw new FriendsApiError(0, "Can't reach the server. Is the backend running?");
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
    throw new FriendsApiError(res.status, message);
  }

  return res.status === 204 ? (undefined as T) : ((await res.json()) as T);
}

export const friendsApi = {
  list: (token: string) => request<FriendSummary[]>("/api/friends", token),

  incoming: (token: string) =>
    request<FriendRequest[]>("/api/friends/requests/incoming", token),

  outgoing: (token: string) =>
    request<FriendRequest[]>("/api/friends/requests/outgoing", token),

  status: (token: string, userId: number) =>
    request<FriendStatus>(`/api/friends/status/${userId}`, token),

  sendRequest: (token: string, addresseeUserId: number) =>
    request<FriendRequest>("/api/friends/requests", token, {
      method: "POST",
      body: JSON.stringify({ addresseeUserId }),
    }),

  accept: (token: string, requestId: number) =>
    request<FriendRequest>(`/api/friends/requests/${requestId}/accept`, token, {
      method: "POST",
    }),

  decline: (token: string, requestId: number) =>
    request<FriendRequest>(`/api/friends/requests/${requestId}/decline`, token, {
      method: "POST",
    }),

  cancel: (token: string, requestId: number) =>
    request<void>(`/api/friends/requests/${requestId}`, token, {
      method: "DELETE",
    }),

  unfriend: (token: string, userId: number) =>
    request<void>(`/api/friends/${userId}`, token, {
      method: "DELETE",
    }),
};
