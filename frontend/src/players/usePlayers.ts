import { useCallback, useEffect, useState } from "react";
import { ApiError, playersApi } from "./api";
import type { PlayerSummary } from "./types";

/** Loads every (user, sport) row once. `PlayersBoard` filters client-side from
 *  there, the same way `useTeams` + `TeamsBoard` already work for teams. */
export function usePlayers() {
  const [rows, setRows] = useState<PlayerSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    playersApi
      .search({ limit: 100 })
      .then(setRows)
      .catch((e: ApiError) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(load, [load]);

  return { rows, loading, error, reload: load };
}