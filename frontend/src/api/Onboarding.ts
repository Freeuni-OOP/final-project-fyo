import { requireCurrentUserId } from "../auth/session";

const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

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
    const userId = requireCurrentUserId();
    const res = await fetch(
        `${BASE}/api/onboarding/status?userId=${userId}`
    );
    if (!res.ok) throw new Error("Failed to fetch onboarding status");
    return res.json();
}

export async function submitOnboarding(
    data: OnboardingPayload
): Promise<OnboardingResponse> {
    const userId = requireCurrentUserId();
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