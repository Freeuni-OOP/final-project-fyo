export interface Sport {
  no: string;
  name: string;
  /** People active this week — drives the league-table bar. */
  n: number;
}

export const SPORTS: Sport[] = [
  { no: "01", name: "Tennis", n: 1240 },
  { no: "02", name: "Football", n: 3580 },
  { no: "03", name: "Basketball", n: 2110 },
  { no: "04", name: "Volleyball", n: 980 },
  { no: "05", name: "Padel", n: 640 },
  { no: "06", name: "Table Tennis", n: 1020 },
  { no: "07", name: "Badminton", n: 510 },
  { no: "08", name: "Running", n: 1870 },
];

export const SPORT_MAX = Math.max(...SPORTS.map((s) => s.n));
