import { Routes, Route, Navigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { getOnboardingStatus } from "./api/onboarding";
import { LandingPage } from "./pages/LandingPage";
import { OnboardingPage } from "./pages/OnboardingPage";
import { TeamsView } from "./teams/TeamsView";

type GuardState = "loading" | "needs-onboarding" | "done";

function OnboardingGuard() {
    const [state, setState] = useState<GuardState>("loading");
    useEffect(() => {
        getOnboardingStatus()
            .then((res) => {
                setState(res.onboardingCompleted ? "done" : "needs-onboarding");
            })
            .catch(() => {
                setState("needs-onboarding");
            });
    }, []);
    if (state === "loading") return null;
    if (state === "needs-onboarding") return <Navigate to="/onboarding" replace />;
    return <Navigate to="/" replace />;
}

export default function App() {
    return (
        <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/onboarding" element={<OnboardingPage />} />
            <Route path="/check" element={<OnboardingGuard />} />
            <Route path="/teams" element={<TeamsView />} />
        </Routes>
    );
}