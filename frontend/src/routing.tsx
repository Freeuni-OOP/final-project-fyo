import { useEffect, useState } from "react";
import { Splash } from "./app/Splash";

export function useHashRoute(): string {
  const [hash, setHash] = useState(() =>
    typeof window !== "undefined" ? window.location.hash : ""
  );

  useEffect(() => {
    const onChange = () => setHash(window.location.hash);
    window.addEventListener("hashchange", onChange);
    return () => window.removeEventListener("hashchange", onChange);
  }, []);

  return hash;
}

/** Match route itself or sub-path/query, not a sibling like `#/login-admin`. */
export function matchesRoute(hash: string, route: string): boolean {
  if (!hash.startsWith(route)) return false;
  const next = hash.charAt(route.length);
  return next === "" || next === "/" || next === "?";
}

/** True for `""`, `"#"` and `"#/"` — every spelling of the site root. */
export function isRoot(hash: string): boolean {
  return hash === "" || hash === "#" || hash === "#/";
}

/**
 * Navigates in an effect rather than during render, so React never sees a
 * state update from another component mid-render.
 */
export function Redirect({ to }: { to: string }) {
  useEffect(() => {
    if (window.location.hash !== to) window.location.hash = to;
  }, [to]);

  return <Splash />;
}
