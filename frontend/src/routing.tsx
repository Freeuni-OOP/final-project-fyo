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
 * The segment right after `route`, or null when the hash *is* the route.
 * `#/app/teams/12` → `"12"`; `#/app/teams` and `#/app/teams/` → `null`.
 */
export function routeParam(hash: string, route: string): string | null {
  if (!hash.startsWith(`${route}/`)) return null;
  const segment = hash.slice(route.length + 1).split(/[/?]/)[0];
  return segment === "" ? null : segment;
}

/** `routeParam` narrowed to a positive integer id — null for `#/teams/abc`. */
export function routeId(hash: string, route: string): number | null {
  const raw = routeParam(hash, route);
  if (raw === null || !/^\d+$/.test(raw)) return null;
  const id = Number(raw);
  return id > 0 ? id : null;
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
