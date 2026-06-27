import { Button } from "../common/Button";
import "./CTA.css";

export function CTA() {
  return (
    <section className="join" id="join">
      <div className="join__inner" data-reveal>
        <h2 className="join__title">
          Your next game is
          <br />
          one request away
        </h2>
        <p className="join__sub">
          Make a profile, find a court near you, and put a name on the other
          side of the net.
        </p>
        <div className="join__actions">
          <Button href="#top" variant="optic">
            Create your profile
          </Button>
          <Button href="#sports" variant="ghost">
            See who's playing →
          </Button>
        </div>
      </div>
    </section>
  );
}
