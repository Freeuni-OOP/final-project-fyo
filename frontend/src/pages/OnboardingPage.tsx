import { useEffect, useState } from "react";
import { useReveal } from "../hooks/useReveal";
import { Wordmark } from "../components/common/Wordmark";
import { OnboardingForm } from "../components/onboarding/OnboardingForm";
import { getSports, type SportDto } from "../api/Sports";
import { submitOnboarding, type OnboardingPayload } from "../api/Onboarding";
import { useAuth } from "../hooks/useAuth";
import { useSession } from "../session/SessionContext";
import "./OnboardingPage.css";

/** Names saved at signup so we don't ask twice (review feedback). */
function readSignupPrefill(): { name: string; surname: string } {
  try {
    return {
      name: sessionStorage.getItem("fyo.signupName") ?? "",
      surname: sessionStorage.getItem("fyo.signupSurname") ?? "",
    };
  } catch {
    return { name: "", surname: "" };
  }
}

export function OnboardingPage() {
  useReveal();

  const { refresh, signOut, user } = useSession();
  const { getIdToken } = useAuth();
  const [sports, setSports] = useState<SportDto[]>([]);
  const [sportsError, setSportsError] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [prefill] = useState(readSignupPrefill);

  useEffect(() => {
    getSports()
      .then(setSports)
      .catch(() => setSportsError(true));
  }, []);

  async function handleSubmit(data: OnboardingPayload) {
    setSubmitting(true);
    setSubmitError(null);
    try {
      const token = await getIdToken();
      if (!token) {
        setSubmitError("You need to sign in to complete onboarding.");
        return;
      }
      await submitOnboarding(token, data);
      try {
        sessionStorage.removeItem("fyo.signupName");
        sessionStorage.removeItem("fyo.signupSurname");
      } catch {
        /* ignore */
      }
      // Must land before the redirect: the session still holds `onboarding:
      // true`, and App bounces anyone carrying that flag back to this page.
      await refresh();
      window.location.hash = "#/app";
    } catch (err) {
      const msg = err instanceof Error ? err.message : "Something went wrong.";
      if (msg.toLowerCase().includes("username")) {
        setSubmitError("That username is already taken — try another one.");
      } else if (msg.toLowerCase().includes("conflict") || msg.toLowerCase().includes("already")) {
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
      <header className="ob-page__bar">
        <Wordmark href="#/" />
        {/* The only way out: every other route redirects back here until the
            profile is finished. */}
        <button type="button" className="ob-page__signout" onClick={signOut}>
          Log out
        </button>
      </header>

      <aside className="ob-page__aside">
        <div className="ob-page__aside-inner">
          <p className="eyebrow eyebrow--invert" data-reveal>
            Player setup
          </p>
          <h1 className="ob-page__aside-title" data-reveal>
            One time.
            <br />
            Then you play.
          </h1>
          <p className="ob-page__aside-lead" data-reveal>
            Set up your profile once and you're in the pool — players and teams
            across the city will be able to find you by sport, level, and region.
          </p>

          <ul className="ob-page__checklist" data-reveal>
            <li>Your name &amp; username</li>
            <li>Age, sex &amp; region</li>
            <li>Sports &amp; skill levels</li>
          </ul>
        </div>
      </aside>

      <main className="ob-page__main">
        {user === null ? (
          <div className="ob-page__load-error">
            <p>You need to sign in to complete onboarding.</p>
            <p>
              <a href="#/login">Log in</a>
              {" · "}
              <a href="#/signup">Sign up</a>
            </p>
          </div>
        ) : sportsError ? (
          <p className="ob-page__load-error">
            Could not load sports. Make sure the backend is running and refresh.
          </p>
        ) : (
          <OnboardingForm
            sports={sports}
            initialName={prefill.name}
            initialSurname={prefill.surname}
            onSubmit={handleSubmit}
            submitting={submitting}
            error={submitError}
          />
        )}
      </main>
    </div>
  );
}
