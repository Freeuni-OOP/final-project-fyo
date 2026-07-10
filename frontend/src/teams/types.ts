/* TypeScript mirror of the backend `com.fyo.team.dto` records
   (branch: team-backend). Keep field names in sync with the Java DTOs. */

export type TeamMemberRole = "CAPTAIN" | "MEMBER";

export interface Sport {
  id: number;
  name: string;
}

export interface UserSummary {
  id: number;
  username: string;
  name: string;
  surname: string;
  region: string | null;
  imageUrl: string | null;
}

export interface TeamSummary {
  id: number;
  name: string;
  sport: Sport;
  region: string | null;
  description: string | null;
  logoUrl: string | null;
  captain: UserSummary;
  maxPlayers: number;
  openSpots: number;
  isRecruiting: boolean;
}

export interface TeamMember {
  id: number;
  userId: number;
  username: string;
  fullName: string;
  imageUrl: string | null;
  role: TeamMemberRole;
  joinedAt: string;
}

export interface TeamDetails extends TeamSummary {
  createdAt: string;
  members: TeamMember[];
}

/** Mirrors `CreateTeamRequest`: name, sportId, maxPlayers and captainUserId are required. */
export interface CreateTeamPayload {
  name: string;
  sportId: number;
  region: string | null;
  description: string | null;
  logoUrl: string | null;
  maxPlayers: number;
  isRecruiting: boolean;
  captainUserId: number;
  /** Players the captain seats up front. The captain is added regardless. */
  memberUserIds: number[];
}

export type JoinRequestStatus = "PENDING" | "ACCEPTED" | "DECLINED";

/** A request as its captain sees it: it names the applicant. */
export interface JoinRequest {
  id: number;
  teamId: number;
  userId: number;
  username: string;
  fullName: string;
  imageUrl: string | null;
  status: JoinRequestStatus;
  createdAt: string;
}

/** The same request as its applicant sees it: it names the team. Never ACCEPTED —
 *  once accepted, the team shows up in `MyTeam` instead. */
export interface MyJoinRequest {
  id: number;
  team: TeamSummary;
  status: JoinRequestStatus;
  createdAt: string;
}

/** A team the signed-in user plays for, and how they relate to it. */
export interface MyTeam {
  team: TeamSummary;
  role: TeamMemberRole;
  joinedAt: string;
}
