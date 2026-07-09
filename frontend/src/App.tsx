import { useEffect, useState } from "react";
import { LandingPage } from "./pages/LandingPage";
import { OnboardingPage } from "./pages/OnboardingPage";
import { TeamsView } from "./teams/TeamsView";
import Login from "./Login";
import Signup from "./Signup";

/**
 * Hash routes (same style as teams):
 *   #/teams, #/onboarding, #/login, #/signup
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

  return <LandingPage />;
}
