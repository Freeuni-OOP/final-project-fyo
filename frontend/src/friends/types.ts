import type { UserSummary } from "../teams/types";

export type FriendRelationshipStatus =
  | "NONE"
  | "PENDING_OUTGOING"
  | "PENDING_INCOMING"
  | "FRIENDS";

export type FriendRequestStatus = "PENDING" | "ACCEPTED" | "DECLINED";

export interface FriendSummary {
  requestId: number;
  user: UserSummary;
  friendsSince: string;
}

export interface FriendRequest {
  id: number;
  requester: UserSummary;
  addressee: UserSummary;
  status: FriendRequestStatus;
  createdAt: string;
}

export interface FriendStatus {
  userId: number;
  status: FriendRelationshipStatus;
  requestId: number | null;
}
