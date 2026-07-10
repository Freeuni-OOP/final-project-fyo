/** Parsed chat hash routes under the app shell, with legacy #/chat support. */
export interface ChatRoute {
  conversationId: number | null;
  matchId: number | null;
}

export function parseChatRoute(hash: string): ChatRoute {
  const matchPath = hash.match(/^#\/(?:app\/)?chat\/match\/(\d+)(?:[/?]|$)/);
  if (matchPath) {
    return { conversationId: null, matchId: Number(matchPath[1]) };
  }

  const conversationPath = hash.match(/^#\/(?:app\/)?chat\/(\d+)(?:[/?]|$)/);
  if (conversationPath) {
    return { conversationId: Number(conversationPath[1]), matchId: null };
  }

  return { conversationId: null, matchId: null };
}

export function chatConversationPath(conversationId: number): string {
  return `#/app/chat/${conversationId}`;
}

export function chatMatchPath(matchId: number): string {
  return `#/app/chat/match/${matchId}`;
}
