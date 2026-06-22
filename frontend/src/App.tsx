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

export default function App() {
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
