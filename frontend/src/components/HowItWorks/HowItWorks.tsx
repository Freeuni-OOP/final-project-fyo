import { STEPS } from "../../data/steps";
import { CourtLines } from "./CourtLines";
import "./HowItWorks.css";

export function HowItWorks() {
  return (
    <section className="court" id="how">
      <CourtLines />
      <div className="court__content">
        <div className="section-head section-head--invert" data-reveal>
          <p className="eyebrow eyebrow--invert">
            From cold profile to live game
          </p>
          <h2 className="section-title section-title--invert">
            Five steps, across the net
          </h2>
        </div>
        <ol className="steps" data-reveal>
          {STEPS.map((s) => (
            <li className="step" key={s.no}>
              <span className="step__no">{s.no}</span>
              <h3 className="step__title">{s.title}</h3>
              <p className="step__body">{s.body}</p>
            </li>
          ))}
        </ol>
      </div>
    </section>
  );
}
