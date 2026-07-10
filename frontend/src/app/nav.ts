export interface NavItem {
  label: string;
  href: string;
  /** Rendered dimmed and unclickable: the endpoint exists, the page doesn't yet. */
  soon?: boolean;
}

export const NAV: NavItem[] = [
  { label: "Dashboard", href: "#/app" },
  { label: "Teams", href: "#/app/teams" },
  { label: "Matches", href: "#/app/matches", soon: true },
  { label: "Profile", href: "#/app/profile", soon: true },
  { label: "Admin", href: "#/app/admin" },
];

export function isNavItemActive(item: NavItem, hash: string): boolean {
  if (item.soon) return false;
  // Dashboard is the shell root, so prefix-matching would light it up everywhere.
  if (item.href === "#/app") return hash === "#/app" || hash === "#/app/";
  return hash === item.href || hash.startsWith(`${item.href}/`);
}
