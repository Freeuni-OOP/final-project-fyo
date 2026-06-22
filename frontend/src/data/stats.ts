export interface Stat {
  value: number;
  suffix: string;
  label: string;
}

export const STATS: Stat[] = [
  { value: 12480, suffix: "", label: "Players on the books" },
  { value: 84300, suffix: "", label: "Matches logged" },
  { value: 38, suffix: "", label: "Districts covered" },
  { value: 1284, suffix: "", label: "Games open this week" },
];
