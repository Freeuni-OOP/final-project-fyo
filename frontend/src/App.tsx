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

/** Minimal hash router. `#/teams` opens the team view; anything else is the
 *  landing page (its in-page anchors like `#sports` keep working). */
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

  return <Landing />;
}
