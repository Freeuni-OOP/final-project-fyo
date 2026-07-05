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

export interface Conversation {
  id: number;
  matchId: number | null;
  participants: ConversationParticipant[];
  lastMessage: ChatMessage | null;
  createdAt: string;
}
