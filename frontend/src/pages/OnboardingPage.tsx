import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useReveal } from "../hooks/useReveal";
import { Wordmark } from "../components/common/Wordmark";
import { OnboardingForm } from "../components/onboarding/OnboardingForm";
import { getSports } from "../api/sports";
import { submitOnboarding, type OnboardingPayload } from "../api/onboarding";
import { type SportDto } from "../api/sports";
import "./OnboardingPage.css";

export function OnboardingPage() {
    const navigate = useNavigate();
    useReveal();

    const [sports, setSports] = useState<SportDto[]>([]);
    const [sportsError, setSportsError] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState<string | null>(null);

    useEffect(() => {
        getSports()
            .then(setSports)
            .catch(() => setSportsError(true));
    }, []);

    async function handleSubmit(data: OnboardingPayload) {
        setSubmitting(true);
        setSubmitError(null);
        try {
            await submitOnboarding(data);
            navigate("/");
        } catch (err) {
            const msg = err instanceof Error ? err.message : "Something went wrong.";
            // Surface conflict errors clearly
            if (msg.toLowerCase().includes("username")) {
                setSubmitError("That username is already taken — try another one.");
            } else if (msg.toLowerCase().includes("conflict")) {
                setSubmitError("Onboarding already completed for this account.");
            } else {
                setSubmitError("Something went wrong. Please try again.");
            }
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <div className="ob-page">
            {/* ── minimal top bar ───────────────────────────────────── */}
            <header className="ob-page__bar">
                <Wordmark href="/" />
            </header>

            {/* ── left panel ────────────────────────────────────────── */}
            <aside className="ob-page__aside">
                <div className="ob-page__aside-inner">
                    <p className="eyebrow eyebrow--invert" data-reveal>
                        Player setup
                    </p>
                    <h1 className="ob-page__aside-title" data-reveal>
                        One time.<br />Then you play.
                    </h1>
                    <p className="ob-page__aside-lead" data-reveal>
                        Set up your profile once and you're in the pool — players and teams
                        across the city will be able to find you by sport, level, and
                        region.
                    </p>

                    <ul className="ob-page__checklist" data-reveal>
                        <li>Your name &amp; username</li>
                        <li>Age, sex &amp; region</li>
                        <li>Sports &amp; skill levels</li>
                    </ul>
                </div>
            </aside>

            {/* ── right panel — form ────────────────────────────────── */}
            <main className="ob-page__main">
                {sportsError ? (
                    <p className="ob-page__load-error">
                        Could not load sports. Make sure the backend is running and refresh.
                    </p>
                ) : (
                    <OnboardingForm
                        sports={sports}
                        onSubmit={handleSubmit}
                        submitting={submitting}
                        error={submitError}
                    />
                )}
            </main>
        </div>
    );
}