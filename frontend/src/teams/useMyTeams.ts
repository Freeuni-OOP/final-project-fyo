import { useCallback, useEffect, useState } from "react";
import { ApiError, teamsApi } from "./api";
import type { MyJoinRequest, MyTeam } from "./types";

/**
 * The signed-in user's own teams and the requests they are still waiting on.
 * Both are fetched together: the two lists are the same story — where you play,
 * and where you have asked to.
 */
export function useMyTeams(userId: number | undefined) {
  const [teams, setTeams] = useState<MyTeam[]>([]);
  const [requests, setRequests] = useState<MyJoinRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => {
    if (userId === undefined) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    Promise.all([teamsApi.myTeams(userId), teamsApi.myJoinRequests(userId)])
      .then(([myTeams, myRequests]) => {
        setTeams(myTeams);
        setRequests(myRequests);
      })
      .catch((e: ApiError) => setError(e.message))
      .finally(() => setLoading(false));
  }, [userId]);

  useEffect(load, [load]);

  return { teams, requests, loading, error, reload: load };
}
