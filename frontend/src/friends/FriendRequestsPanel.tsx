import { useCallback, useEffect, useState } from "react";
import { useAuth } from "../hooks/useAuth";
import { Avatar } from "../teams/ui";
import { FriendsApiError, friendsApi } from "./api";
import type { FriendRequest } from "./types";
import "./friends.css";

function fullName(request: FriendRequest): string {
  const user = request.requester;
  return `${user.name} ${user.surname}`.trim() || user.username;
}

export function FriendRequestsPanel() {
  const { getIdToken } = useAuth();
  const [incoming, setIncoming] = useState<FriendRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const token = await getIdToken();
      if (!token) {
        setIncoming([]);
        return;
      }
      const list = await friendsApi.incoming(token);
      setIncoming(list);
    } catch (err) {
      setError(err instanceof FriendsApiError ? err.message : "Failed to load requests");
    } finally {
      setLoading(false);
    }
  }, [getIdToken]);

  useEffect(() => {
    void load();
  }, [load]);

  async function respond(requestId: number, action: "accept" | "decline") {
    try {
      const token = await getIdToken();
      if (!token) return;
      if (action === "accept") await friendsApi.accept(token, requestId);
      else await friendsApi.decline(token, requestId);
      await load();
    } catch (err) {
      setError(err instanceof FriendsApiError ? err.message : "Action failed");
    }
  }

  return (
    <section className="panel">
      <div className="panel__head">
        <h2 className="panel__title">Friend requests</h2>
        <a className="panel__link" href="#/app/friends">
          Friends →
        </a>
      </div>

      {loading && <p className="friends__hint">Loading…</p>}
      {error && !loading && <p className="friends__error">{error}</p>}
      {!loading && !error && incoming.length === 0 && (
        <p className="friends__hint">No pending friend requests.</p>
      )}

      {!loading && incoming.length > 0 && (
        <ul className="friends__dashboard-list">
          {incoming.slice(0, 3).map((request) => (
            <li className="friends__dashboard-row" key={request.id}>
              <a className="friends__profile-link" href={`#/profile/${request.requester.id}`}>
                <Avatar
                  src={request.requester.imageUrl}
                  name={fullName(request)}
                  size={36}
                />
                <span className="friends__dashboard-copy">
                  <strong>{fullName(request)}</strong>
                  <span>@{request.requester.username}</span>
                </span>
              </a>
              <div className="friends__dashboard-actions">
                <button type="button" onClick={() => void respond(request.id, "accept")}>
                  Accept
                </button>
                <button
                  type="button"
                  className="ghost"
                  onClick={() => void respond(request.id, "decline")}
                >
                  Decline
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
