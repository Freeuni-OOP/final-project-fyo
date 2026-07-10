import type {
  AcceptListingResult,
  CreateListingRequest,
  ListingResponse,
  MatchListing,
} from "./types";

const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

export class ListingsApiError extends Error {
  constructor(
    public status: number,
    message: string
  ) {
    super(message);
    this.name = "ListingsApiError";
  }
}

async function request<T>(
  path: string,
  token: string | null,
  init?: RequestInit
): Promise<T> {
  let res: Response;
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  if (token) headers.Authorization = `Bearer ${token}`;

  try {
    res = await fetch(`${BASE}${path}`, {
      ...init,
      headers: { ...headers, ...(init?.headers as Record<string, string> | undefined) },
    });
  } catch {
    throw new ListingsApiError(0, "Can't reach the server. Is the backend running?");
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
    throw new ListingsApiError(res.status, message);
  }

  return res.status === 204 ? (undefined as T) : ((await res.json()) as T);
}

export const listingsApi = {
  browse: (sportId?: number) => {
    const qs = sportId != null ? `?sportId=${sportId}` : "";
    return request<MatchListing[]>(`/api/match-listings${qs}`, null);
  },

  create: (token: string, body: CreateListingRequest) =>
    request<MatchListing>("/api/match-listings", token, {
      method: "POST",
      body: JSON.stringify(body),
    }),

  respond: (token: string, listingId: number, teamId?: number | null) =>
    request<ListingResponse>(`/api/match-listings/${listingId}/responses`, token, {
      method: "POST",
      body: JSON.stringify({ teamId: teamId ?? null }),
    }),

  pendingResponses: (token: string, listingId: number) =>
    request<ListingResponse[]>(`/api/match-listings/${listingId}/responses`, token),

  accept: (token: string, listingId: number, responseId: number) =>
    request<AcceptListingResult>(
      `/api/match-listings/${listingId}/responses/${responseId}/accept`,
      token,
      { method: "POST" }
    ),

  decline: (token: string, listingId: number, responseId: number) =>
    request<ListingResponse>(
      `/api/match-listings/${listingId}/responses/${responseId}/decline`,
      token,
      { method: "POST" }
    ),
};
