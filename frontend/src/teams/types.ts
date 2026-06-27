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
