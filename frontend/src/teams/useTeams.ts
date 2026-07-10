import { useCallback, useEffect, useState } from "react";
import { ApiError, teamsApi } from "./api";
import type { TeamDetails, TeamSummary } from "./types";

/** Loads the team list. Roster changes happen on a team's own page, which
 *  remounts this hook on the way back, so the list re-reads itself. */
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

  const addTeam = useCallback((created: TeamDetails) => {
    setTeams((prev) => [created, ...prev]);
  }, []);

  return { teams, loading, error, reload: load, addTeam };
}
