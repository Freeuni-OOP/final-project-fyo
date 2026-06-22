import { type CSSProperties } from "react";
import { SPORTS, SPORT_MAX } from "../../data/sports";
import "./Sports.css";

export function Sports() {
  return (
    <section className="sports" id="sports">
      <div className="section-head" data-reveal>
        <h2 className="section-title">Pick a sport. One question: who's in?</h2>
        <p className="section-lead">
          Every sport has its own pool of players and teams looking right now,
          and the list keeps growing. Numbers are people active this week.
        </p>
      </div>

      <ul className="fixtures" data-reveal>
        <li className="fixtures__head">
          <span>Sport</span>
          <span className="fixtures__head-active">Active this week</span>
          <span />
        </li>
        {SPORTS.map((s, i) => (
          <li className="fixtures__row" key={s.no}>
            <span className="fixtures__name">
              <span className="fixtures__no">{s.no}</span>
              {s.name}
            </span>
            <span className="fixtures__meter">
              <span className="meter__track">
                <span
                  className="meter__fill"
                  style={
                    {
                      "--w": `${Math.round((s.n / SPORT_MAX) * 100)}%`,
                      transitionDelay: `${i * 55}ms`,
                    } as CSSProperties
                  }
                />
              </span>
              <span className="fixtures__count">
                {s.n.toLocaleString("en-US")}
              </span>
            </span>
            <a
              className="fixtures__go"
              href="#join"
              aria-label={`Find ${s.name}`}
            >
              Find →
            </a>
          </li>
        ))}
      </ul>
    </section>
  );
}
