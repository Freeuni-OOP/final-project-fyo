const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

/** Thrown for any failed auth request; carries the HTTP status (0 = network). */
export class AuthApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = "AuthApiError";
  }
}

export interface AuthUser {
  id: number;
  firebaseUid: string;
  username: string;
  email: string;
  name: string;
  surname: string;
  imageUrl: string | null;
  /** True while the account still has to finish profile setup. The onboarding
   *  flow (POST /api/onboarding — feature/onboarding-v2 until it merges)
   *  completes it; route users with onboarding=true there after auth. */
  onboarding: boolean;
  admin: boolean;
}

/**
 * Calls a backend auth endpoint. Identity travels only in the Bearer token;
 * the optional body carries extra profile fields (never email/uid).
 */
export async function authRequest(
  path: "/api/auth/signup" | "/api/auth/login",
  idToken: string,
  body?: unknown
): Promise<AuthUser> {
  let res: Response;
  try {
    res = await fetch(`${BASE}${path}`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${idToken}`,
        ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
      },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch (err) {
    console.error(`Auth request ${path} never reached the server:`, err);
    throw new AuthApiError(
      0,
      "Can't reach the server — check your internet connection and that the backend is running, then try again."
    );
  }

  if (!res.ok) {
    // Read the body once as text: it is logged raw for debugging, and only a
    // well-formed { message } from it is ever shown to the user.
    const raw = await res.text().catch(() => "");
    console.error(`Auth request ${path} failed with ${res.status}:`, raw || "<empty body>");

    let message: string | null = null;
    try {
      const data = JSON.parse(raw);
      if (typeof data?.message === "string" && data.message) message = data.message;
    } catch {
      /* non-JSON error body — fall back to a status-based message below */
    }
    if (!message) {
      message =
        res.status >= 500
          ? `Server error (${res.status}). Please try again in a moment — contact support if it keeps happening.`
          : `Request failed (${res.status}${res.statusText ? ` ${res.statusText}` : ""}).`;
    }
    throw new AuthApiError(res.status, message);
  }

  return (await res.json()) as AuthUser;
}
