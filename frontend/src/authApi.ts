const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

export interface AuthUser {
  id: number;
  firebaseUid: string;
  username: string;
  email: string;
  name: string;
  surname: string;
  imageUrl: string | null;
  onboarding: boolean;
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
  } catch {
    throw new Error("Can't reach the server. Is the backend running?");
  }

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const data = await res.json();
      if (data?.message) message = data.message;
    } catch {
      /* non-JSON error body — keep the default message */
    }
    throw new Error(message);
  }

  return (await res.json()) as AuthUser;
}
