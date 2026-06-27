import { CountUp } from "../common/CountUp";
import { STATS } from "../../data/stats";
import "./Scoreboard.css";

export function Scoreboard() {
  return (
    <section className="board" aria-label="Platform numbers">
      <div className="board__grid" data-reveal>
        {STATS.map((s) => (
          <div className="board__cell" key={s.label}>
            <span className="board__num">
              <CountUp end={s.value} suffix={s.suffix} />
            </span>
            <span className="board__label">{s.label}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
