import { Ball } from "../common/Ball";
import { TICKER } from "../../data/ticker";
import "./Ticker.css";

export function Ticker() {
  // Doubled so the marquee can loop seamlessly at -50%.
  const items = [...TICKER, ...TICKER];

  return (
    <div className="ticker" aria-hidden="true">
      <div className="ticker__track">
        {items.map((text, i) => (
          <span className="ticker__item" key={i}>
            <Ball className="ball--tick" />
            {text}
          </span>
        ))}
      </div>
    </div>
  );
}
