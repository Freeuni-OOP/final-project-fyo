import { FEATURES } from "../../data/features";
import "./Features.css";

export function Features() {
  return (
    <section className="features" id="teams">
      <div className="section-head" data-reveal>
        <h2 className="section-title">
          Built for the way amateurs actually play
        </h2>
        <p className="section-lead">
          Solo or as a captain, the loop is the same: find, play, record, repeat.
        </p>
      </div>

      <ul className="flist" data-reveal>
        {FEATURES.map((f, i) => (
          <li className="frow" key={f.k}>
            <div className="frow__lead">
              <span className="frow__no">{String(i + 1).padStart(2, "0")}</span>
              <div>
                <span className="frow__k">{f.k}</span>
                <h3 className="frow__title">{f.title}</h3>
              </div>
            </div>
            <p className="frow__body">{f.body}</p>
          </li>
        ))}
      </ul>
    </section>
  );
}
