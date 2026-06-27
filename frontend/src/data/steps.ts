export interface Step {
  no: string;
  title: string;
  body: string;
}

export const STEPS: Step[] = [
  {
    no: "01",
    title: "Pick your sport",
    body: "Choose your sport, set your level, and tell us where you usually play.",
  },
  {
    no: "02",
    title: "Scout the field",
    body: "Browse player and team profiles near you: level, record, and real ratings.",
  },
  {
    no: "03",
    title: "Send a request",
    body: "See someone worth playing? Request a match. The net is the ask.",
  },
  {
    no: "04",
    title: "Match & chat",
    body: "When they accept, a chat opens. Settle the time, the place, the format.",
  },
  {
    no: "05",
    title: "Play & rate",
    body: "Log the final score, rate each other, and build a record that follows you.",
  },
];
