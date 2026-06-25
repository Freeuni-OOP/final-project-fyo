export interface Feature {
  k: string;
  title: string;
  body: string;
}

export const FEATURES: Feature[] = [
  {
    k: "Profiles",
    title: "Profiles that earn trust",
    body: "Level, active sports, location, and ratings left by people you've actually played. No bots, no padding.",
  },
  {
    k: "Results",
    title: "Box scores, kept",
    body: "Log the final score after every game. Results roll up into your profile and the standings.",
  },
  {
    k: "Teams",
    title: "Captain's tools",
    body: "Run a squad: schedule matches against other teams, post openings, and manage your roster.",
  },
  {
    k: "Roster",
    title: "Fill the lineup",
    body: "Short two players before kickoff? FYO pings nearby players who fit, and they sub in for the day.",
  },
];
