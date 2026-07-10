export interface UserAdmin {
  id: number;
  username: string;
  name: string;
  surname: string;
  email: string;
  region: string | null;
  imageUrl: string | null;
  admin: boolean;
  archived: boolean;
  createdAt: string;
  archivedAt: string | null;
}

export interface TeamAdmin {
  id: number;
  name: string;
  sportName: string;
  region: string | null;
  captainUsername: string;
  maxPlayers: number;
  openSpots: number;
  recruiting: boolean;
  archived: boolean;
  createdAt: string;
}

export interface SportAdmin {
  id: number;
  sportName: string;
}