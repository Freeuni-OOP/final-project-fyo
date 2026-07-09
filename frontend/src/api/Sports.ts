const BASE = "http://localhost:8081";

export interface SportDto {
    id: number;
    sportName: string;
}

// Hardcoded from V2__insert_sports.sql seed order.
// TODO: replace with GET /api/sports once that endpoint is added.
const SEEDED_SPORTS: SportDto[] = [
    { id: 1,  sportName: "Football"     },
    { id: 2,  sportName: "Basketball"   },
    { id: 3,  sportName: "Tennis"       },
    { id: 4,  sportName: "Volleyball"   },
    { id: 5,  sportName: "Badminton"    },
    { id: 6,  sportName: "Table Tennis" },
    { id: 7,  sportName: "Swimming"     },
    { id: 8,  sportName: "Running"      },
    { id: 9,  sportName: "Padel"        },
    { id: 10, sportName: "Boxing"       },
    { id: 11, sportName: "MMA"          },
    { id: 12, sportName: "Gym"          },
];

export async function getSports(): Promise<SportDto[]> {
    return Promise.resolve(SEEDED_SPORTS);
}