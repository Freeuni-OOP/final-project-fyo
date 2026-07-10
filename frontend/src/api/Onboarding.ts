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

async function authedJson<T>(
    path: string,
    token: string,
    init?: RequestInit
): Promise<T> {
    const res = await fetch(`${BASE}${path}`, {
        ...init,
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
            ...(init?.headers as Record<string, string> | undefined),
        },
    });

    if (res.status === 409) {
        const err = await res.json().catch(() => ({}));
        throw new Error((err as { detail?: string }).detail ?? "Conflict");
    }

    if (!res.ok) {
        let message = "Request failed";
        try {
            const body = await res.json();
            if (body?.message) message = body.message;
            else if (body?.detail) message = body.detail;
        } catch {
            /* keep default */
        }
        throw new Error(message);
    }

    return res.json() as Promise<T>;
}

export async function getOnboardingStatus(token: string): Promise<OnboardingStatusResponse> {
    return authedJson<OnboardingStatusResponse>("/api/onboarding/status", token);
}

export async function submitOnboarding(
    token: string,
    data: OnboardingPayload
): Promise<OnboardingResponse> {
    return authedJson<OnboardingResponse>("/api/onboarding", token, {
        method: "POST",
        body: JSON.stringify(data),
    });
}
