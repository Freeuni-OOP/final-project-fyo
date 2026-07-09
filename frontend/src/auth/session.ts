export const CURRENT_USER_ID_KEY = "fyo.currentUserId";

export class NotAuthenticatedError extends Error {
  constructor() {
    super("No signed-in user id found. Sign in again and retry.");
    this.name = "NotAuthenticatedError";
  }
}

function parseStoredUserId(raw: string | null): number | null {
  if (!raw) return null;
  const id = Number(raw);
  if (Number.isInteger(id) && id > 0) {
    return id;
  }
  return null;
}

export function storeCurrentUserId(userId: number): void {
  const value = String(userId);
  try {
    localStorage.setItem(CURRENT_USER_ID_KEY, value);
    sessionStorage.setItem(CURRENT_USER_ID_KEY, value);
  } catch {
    /* storage can be unavailable in private mode */
  }
}

export function readCurrentUserId(): number | null {
  try {
    const fromLocal = parseStoredUserId(localStorage.getItem(CURRENT_USER_ID_KEY));
    if (fromLocal !== null) {
      return fromLocal;
    }
    return parseStoredUserId(sessionStorage.getItem(CURRENT_USER_ID_KEY));
  } catch {
    /* storage can be unavailable in private mode or during SSR */
    return null;
  }
}

export function requireCurrentUserId(): number {
  const userId = readCurrentUserId();
  if (userId === null) {
    throw new NotAuthenticatedError();
  }
  return userId;
}

export function clearCurrentUserId(): void {
  try {
    localStorage.removeItem(CURRENT_USER_ID_KEY);
    sessionStorage.removeItem(CURRENT_USER_ID_KEY);
  } catch {
    /* ignore storage failures */
  }
}
