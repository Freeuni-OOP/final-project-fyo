export interface ConversationParticipant {
  userId: number;
  username: string;
  fullName: string;
  imageUrl: string | null;
}

export interface ChatMessage {
  id: number;
  conversationId: number;
  senderId: number;
  senderUsername: string;
  body: string;
  createdAt: string;
  readAt: string | null;
}

export type ConversationType = "MATCH" | "DIRECT" | "TEAM";

export interface Conversation {
  id: number;
  type: ConversationType;
  matchId: number | null;
  teamId: number | null;
  participants: ConversationParticipant[];
  lastMessage: ChatMessage | null;
  createdAt: string;
}
