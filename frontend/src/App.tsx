import { useEffect, useState } from "react";
import { LandingPage } from "./pages/LandingPage";
import { OnboardingPage } from "./pages/OnboardingPage";
import { ProfilePage } from "./profile/ProfilePage";
import { PublicProfilePage } from "./profile/PublicProfilePage";
import { TeamsView } from "./teams/TeamsView";
import Login from "./Login";
import Signup from "./Signup";

/**
 * Hash routes (same style as teams):
 *   #/teams, #/onboarding, #/login, #/signup, #/profile, #/profile/:id
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

/** Match route itself or sub-path/query, not a sibling like `#/login-admin`. */
function matchesRoute(hash: string, route: string): boolean {
  if (!hash.startsWith(route)) return false;
  const next = hash.charAt(route.length);
  return next === "" || next === "/" || next === "?";
}

function isPublicProfileRoute(hash: string): boolean {
  return /^#\/profile\/\d+(?:[/?]|$)/.test(hash);
}

export default function App() {
  const hash = useHashRoute();

  if (matchesRoute(hash, "#/teams")) {
    return <TeamsView />;
  }

  if (matchesRoute(hash, "#/onboarding")) {
    return <OnboardingPage />;
  }

  if (matchesRoute(hash, "#/login")) {
    return <Login />;
  }

  if (matchesRoute(hash, "#/signup")) {
    return <Signup />;
  }

  // Public profile must be checked before the bare #/profile route.
  if (isPublicProfileRoute(hash)) {
    return <PublicProfilePage />;
  }

  if (matchesRoute(hash, "#/profile")) {
    return <ProfilePage />;
  }

  return <LandingPage />;
}
