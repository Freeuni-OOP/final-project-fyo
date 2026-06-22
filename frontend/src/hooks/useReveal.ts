import { useEffect } from "react";
import { prefersReducedMotion } from "../utils/motion";

/**
 * Adds the `is-in` class to every `[data-reveal]` element once it scrolls
 * into view, driving the fade-up reveal. Honours reduced-motion by showing
 * everything immediately.
 */
export function useReveal(): void {
  useEffect(() => {
    const nodes = Array.from(
      document.querySelectorAll<HTMLElement>("[data-reveal]")
    );

    if (prefersReducedMotion()) {
      nodes.forEach((n) => n.classList.add("is-in"));
      return;
    }

    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("is-in");
            io.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.18, rootMargin: "0px 0px -8% 0px" }
    );

    nodes.forEach((n) => io.observe(n));
    return () => io.disconnect();
  }, []);
}
