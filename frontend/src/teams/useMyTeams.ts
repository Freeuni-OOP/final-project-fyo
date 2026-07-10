import { useCallback, useEffect, useState } from "react";
import { useAuth } from "../hooks/useAuth";
import { ApiError, teamsApi } from "./api";
import type { MyJoinRequest, MyTeam } from "./types";

/**
 * The signed-in user's own teams and the requests they are still waiting on.
 * Both are fetched together: the two lists are the same story — where you play,
 * and where you have asked to.
 */
export function useMyTeams(userId: number | undefined) {
  const { getIdToken } = useAuth();
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
    (async () => {
      try {
        const token = await getIdToken();
        if (!token) throw new ApiError(401, "Your session expired. Sign in again.");
        const [myTeams, myRequests] = await Promise.all([
          teamsApi.myTeams(token),
          teamsApi.myJoinRequests(token),
        ]);
        setTeams(myTeams);
        setRequests(myRequests);
      } catch (e: unknown) {
        setError(e instanceof ApiError ? e.message : "Could not load your teams.");
      } finally {
        setLoading(false);
      }
    })();
  }, [userId, getIdToken]);

  useEffect(load, [load]);

  return { teams, requests, loading, error, reload: load };
}
