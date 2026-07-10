import { chatApi } from "../chat/api";

const OPEN_CHAT_KEY = "fyo.openChatId";

/** Opens an existing or new direct thread without changing chat internals. */
export async function openDirectChat(token: string, otherUserId: number): Promise<void> {
  const conversation = await chatApi.createDirect(token, otherUserId);
  sessionStorage.setItem(OPEN_CHAT_KEY, String(conversation.id));
  window.location.hash = "#/chat";
}

export async function openTeamChat(token: string, teamId: number): Promise<void> {
  const conversation = await chatApi.createTeam(token, teamId);
  sessionStorage.setItem(OPEN_CHAT_KEY, String(conversation.id));
  window.location.hash = "#/chat";
}

export function consumeOpenChatId(): number | null {
  const raw = sessionStorage.getItem(OPEN_CHAT_KEY);
  if (!raw) return null;
  sessionStorage.removeItem(OPEN_CHAT_KEY);
  const id = Number(raw);
  return Number.isInteger(id) && id > 0 ? id : null;
}
