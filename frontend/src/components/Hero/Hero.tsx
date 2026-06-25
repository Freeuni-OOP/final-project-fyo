import { Button } from "../common/Button";
import "./Hero.css";

export function Hero() {
  return (
    <section className="hero" id="top">
      <p className="eyebrow" data-reveal>
        Tbilisi · pickup sports · open now
      </p>
      <h1 className="hero__title" data-reveal>
        Every player
        <br />
        needs a <span className="hero__accent">match</span>
      </h1>

      <div className="hero__lower">
        <p className="hero__sub" data-reveal>
          FYO connects players and teams across the city. Pick a sport, find a
          partner, an opponent, or a full squad, then chat, schedule, and settle
          it on the court.
        </p>
        <div className="hero__col" data-reveal>
          <div className="hero__actions">
            <Button href="#join" variant="solid">
              Find a game
            </Button>
            <Button href="#sports" variant="ghost">
              Browse players →
            </Button>
          </div>
          <p className="hero__live">
            <span className="dot" /> 1,284 games open this week
          </p>
        </div>
      </div>
    </section>
  );
}
