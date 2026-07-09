import { useEffect, useState } from "react";
import { LandingPage } from "./pages/LandingPage";
import { OnboardingPage } from "./pages/OnboardingPage";
import { TeamsView } from "./teams/TeamsView";

/**
 * Same style as the rest of the app: hash routes.
 *   #/teams       → teams list
 *   #/onboarding  → profile setup form
 *   anything else → landing (in-page anchors like #sports still work)
 */
function useHashRoute(): string {
  const [hash, setHash] = useState(() =>
    typeof window !== "undefined" ? window.location.hash : ""
  );

  useEffect(() => {
    const onChange = () => setHash(window.location.hash);
    window.addEventListener("hashchange", onChange);
    return () => window.removeEventListener("hashchange", onChange);
  }, []);

  return hash;
}

export default function App() {
  const hash = useHashRoute();

  if (hash.startsWith("#/teams")) {
    return <TeamsView />;
  }

  if (hash.startsWith("#/onboarding")) {
    return <OnboardingPage />;
  }

  return <LandingPage />;
}
