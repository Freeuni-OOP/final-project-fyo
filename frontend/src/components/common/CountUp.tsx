import { useEffect, useRef, useState } from "react";
import { prefersReducedMotion } from "../../utils/motion";

interface CountUpProps {
  end: number;
  suffix?: string;
}

/** Animates from 0 to `end` once it scrolls into view. */
export function CountUp({ end, suffix = "" }: CountUpProps) {
  const ref = useRef<HTMLSpanElement>(null);
  const [val, setVal] = useState(0);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    if (prefersReducedMotion()) {
      setVal(end);
      return;
    }

    let raf = 0;
    let started = false;

    const run = (t0: number) => {
      const dur = 1100;
      const tick = (t: number) => {
        const p = Math.min(1, (t - t0) / dur);
        const eased = 1 - Math.pow(1 - p, 3);
        setVal(Math.round(end * eased));
        if (p < 1) raf = requestAnimationFrame(tick);
      };
      raf = requestAnimationFrame(tick);
    };

    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting && !started) {
            started = true;
            run(performance.now());
            io.disconnect();
          }
        });
      },
      { threshold: 0.5 }
    );

    io.observe(el);
    return () => {
      io.disconnect();
      cancelAnimationFrame(raf);
    };
  }, [end]);

  return (
    <span ref={ref}>
      {val.toLocaleString("en-US")}
      {suffix}
    </span>
  );
}
