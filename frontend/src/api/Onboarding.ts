import { requireCurrentUserId } from "../auth/session";

const CURRENT_USER_ID_KEY = "fyo.currentUserId";

function readCurrentUserId(): number {
    try {
        const raw = sessionStorage.getItem(CURRENT_USER_ID_KEY) ?? localStorage.getItem(CURRENT_USER_ID_KEY);
        const id = raw ? Number(raw) : NaN;
        if (Number.isInteger(id) && id > 0) {
            return id;
        }
    } catch {
        /* storage can be unavailable in private mode or during SSR */
    }

    throw new Error("No signed-in user id found. Sign in again and retry onboarding.");
}

export interface UserSportPayload {
    sportId: number;
    skillLevel: "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
}

export interface OnboardingPayload {
    name: string;
    surname: string;
    username: string;
    age: number;
    sex: "MALE" | "FEMALE" | "OTHER";
    region: string;
    imageUrl: string;
    sports: UserSportPayload[];
}

export interface OnboardingStatusResponse {
    onboardingCompleted: boolean;
}

export interface OnboardingResponse {
    id: number;
    name: string;
    surname: string;
    username: string;
    age: number;
    sex: string;
    region: string;
    imageUrl: string;
    onboardingCompleted: boolean;
    sports: { sportId: number; sportName: string; skillLevel: string }[];
}

export async function getOnboardingStatus(): Promise<OnboardingStatusResponse> {
    const userId = readCurrentUserId();
    const res = await fetch(
        `${BASE}/api/onboarding/status?userId=${userId}`
    );
    if (!res.ok) throw new Error("Failed to fetch onboarding status");
    return res.json();
}

export async function submitOnboarding(
    data: OnboardingPayload
): Promise<OnboardingResponse> {
    const userId = readCurrentUserId();
    const res = await fetch(`${BASE}/api/onboarding?userId=${userId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
    });

    if (res.status === 409) {
        const err = await res.json().catch(() => ({}));
        // Spring sends { detail: "..." } for ResponseStatusException
        throw new Error(err.detail ?? "Conflict");
    }

    if (!res.ok) throw new Error("Submission failed");
    return res.json();
}