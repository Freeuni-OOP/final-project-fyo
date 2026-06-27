import { useEffect } from "react";

/**
 * Adds `is-in` to every `[data-reveal]` element inside the court once it
 * scrolls into view (fade-up reveal). Honours reduced-motion. Re-runs when
 * `deps` change so freshly-rendered rows get observed too.
 */
export function useReveal(deps: unknown[] = []): void {
  useEffect(() => {
    const nodes = Array.from(
      document.querySelectorAll<HTMLElement>(".court [data-reveal]:not(.is-in)")
    );
    if (nodes.length === 0) return;

    const reduce =
      typeof window !== "undefined" &&
      window.matchMedia?.("(prefers-reduced-motion: reduce)").matches;

    if (reduce) {
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
      { threshold: 0.15, rootMargin: "0px 0px -6% 0px" }
    );

    nodes.forEach((n) => io.observe(n));
    return () => io.disconnect();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);
}
