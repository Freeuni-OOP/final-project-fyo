import { useEffect, useState } from "react";
import { useReveal } from "./hooks/useReveal";
import { TopBar } from "./components/TopBar/TopBar";
import { Ticker } from "./components/Ticker/Ticker";
import { Hero } from "./components/Hero/Hero";
import { Sports } from "./components/Sports/Sports";
import { HowItWorks } from "./components/HowItWorks/HowItWorks";
import { Features } from "./components/Features/Features";
import { Scoreboard } from "./components/Scoreboard/Scoreboard";
import { CTA } from "./components/CTA/CTA";
import { Footer } from "./components/Footer/Footer";
import { TeamsView } from "./teams/TeamsView";
import Login from "./Login";
import Signup from "./Signup";

function Landing() {
  useReveal();

  return (
    <main className="app-shell">
      <TopBar />
      <Ticker />
      <Hero />
      <Sports />
      <HowItWorks />
      <Features />
      <Scoreboard />
      <CTA />
      <Footer />
    </main>
  );
}

/** Minimal hash router. `#/teams`, `#/login` and `#/signup` open their views;
 *  anything else is the landing page (its in-page anchors keep working). */
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

/** True when the hash addresses `route` itself or a sub-path/query under it
 *  (`#/login`, `#/login/x`, `#/login?y`) — but not a sibling like
 *  `#/login-admin`, which a plain startsWith would swallow. */
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

  if (matchesRoute(hash, "#/login")) {
    return <Login />;
  }

  if (matchesRoute(hash, "#/signup")) {
    return <Signup />;
  }

  return <Landing />;
}
