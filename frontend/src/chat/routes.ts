/** Parsed `#/chat` hash routes. */
export interface ChatRoute {
  conversationId: number | null;
  matchId: number | null;
}

export function parseChatRoute(hash: string): ChatRoute {
  const matchPath = hash.match(/^#\/chat\/match\/(\d+)(?:[/?]|$)/);
  if (matchPath) {
    return { conversationId: null, matchId: Number(matchPath[1]) };
  }

  const conversationPath = hash.match(/^#\/chat\/(\d+)(?:[/?]|$)/);
  if (conversationPath) {
    return { conversationId: Number(conversationPath[1]), matchId: null };
  }

  return { conversationId: null, matchId: null };
}

export function chatConversationPath(conversationId: number): string {
  return `#/chat/${conversationId}`;
}

export function chatMatchPath(matchId: number): string {
  return `#/chat/match/${matchId}`;
}
