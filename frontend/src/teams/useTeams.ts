import { useCallback, useEffect, useState } from "react";
import { ApiError, teamsApi } from "./api";
import type { TeamDetails, TeamSummary } from "./types";

/** Loads the team list and keeps it in sync with roster changes made in the drawer. */
export function useTeams() {
  const [teams, setTeams] = useState<TeamSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    teamsApi
      .list()
      .then(setTeams)
      .catch((e: ApiError) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(load, [load]);

  const applyRosterChange = useCallback((updated: TeamDetails) => {
    setTeams((prev) =>
      prev.map((t) =>
        t.id === updated.id
          ? { ...t, openSpots: updated.openSpots, isRecruiting: updated.isRecruiting }
          : t
      )
    );
  }, []);

  const addTeam = useCallback((created: TeamDetails) => {
    setTeams((prev) => [created, ...prev]);
  }, []);

  return { teams, loading, error, reload: load, applyRosterChange, addTeam };
}
