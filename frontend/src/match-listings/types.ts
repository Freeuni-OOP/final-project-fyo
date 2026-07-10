export type MatchFormat = "ONE_VS_ONE" | "TEAM_VS_TEAM";
export type MatchListingStatus = "OPEN" | "FILLED" | "CANCELLED";
export type ListingResponseStatus = "PENDING" | "ACCEPTED" | "DECLINED";

export interface MatchParticipant {
  userId: number | null;
  teamId: number | null;
  displayName: string;
  imageUrl: string | null;
}

export interface MatchListing {
  id: number;
  sport: { id: number; name: string };
  format: MatchFormat;
  postedBy: MatchParticipant;
  location: string | null;
  proposedDatetime: string | null;
  status: MatchListingStatus;
  matchId: number | null;
  createdAt: string;
}

export interface ListingResponse {
  id: number;
  listingId: number;
  responder: MatchParticipant;
  status: ListingResponseStatus;
  createdAt: string;
}

export interface AcceptListingResult {
  listingId: number;
  matchId: number;
  conversationId: number;
}

export interface CreateListingRequest {
  sportId: number;
  teamId?: number | null;
  location?: string | null;
  proposedDatetime?: string | null;
}
