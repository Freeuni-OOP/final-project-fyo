import { useCallback, useEffect, useMemo, useState } from "react";
import { useAuth } from "../hooks/useAuth";
import { useSession } from "../session/SessionContext";
import { PageHead } from "../app/AppShell";
import { Avatar } from "../teams/ui";
import { FriendsApiError, friendsApi } from "./api";
import { FriendSearch } from "./FriendSearch";
import { FriendActionButton } from "./FriendActionButton";
import type { FriendRequest, FriendSummary } from "./types";
import type { UserSummary } from "../teams/types";
import "./friends.css";

function fullName(user: UserSummary): string {
  return `${user.name} ${user.surname}`.trim() || user.username;
}

export function FriendsPage() {
  const { user } = useSession();
  const { getIdToken } = useAuth();
  const [friends, setFriends] = useState<FriendSummary[]>([]);
  const [incoming, setIncoming] = useState<FriendRequest[]>([]);
  const [outgoing, setOutgoing] = useState<FriendRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setError(null);
    try {
      const token = await getIdToken();
      if (!token) throw new FriendsApiError(401, "Sign in to view friends.");
      const [friendList, incomingList, outgoingList] = await Promise.all([
        friendsApi.list(token),
        friendsApi.incoming(token),
        friendsApi.outgoing(token),
      ]);
      setFriends(friendList);
      setIncoming(incomingList);
      setOutgoing(outgoingList);
    } catch (err) {
      setError(err instanceof FriendsApiError ? err.message : "Failed to load friends");
    } finally {
      setLoading(false);
    }
  }, [getIdToken, user]);

  useEffect(() => {
    void load();
  }, [load]);

  const friendIds = useMemo(
    () => new Set(friends.map((friend) => friend.user.id)),
    [friends]
  );

  async function sendRequestTo(userSummary: UserSummary) {
    try {
      const token = await getIdToken();
      if (!token) throw new FriendsApiError(401, "Sign in to send requests.");
      await friendsApi.sendRequest(token, userSummary.id);
      await load();
    } catch (err) {
      setError(err instanceof FriendsApiError ? err.message : "Could not send request");
    }
  }

  if (!user) return null;

  return (
    <>
      <PageHead eyebrow="Social" title="Friends" />

      {error && (
        <p className="friends__error" role="alert">
          {error}
        </p>
      )}

      <FriendSearch
        currentUserId={user.id}
        friendIds={friendIds}
        onRequestSent={(found) => void sendRequestTo(found)}
      />

      <section className="panel friends__panel">
        <div className="panel__head">
          <h2 className="panel__title">Incoming requests</h2>
          <span className="friends__count">{incoming.length}</span>
        </div>
        {loading && <p className="friends__hint">Loading…</p>}
        {!loading && incoming.length === 0 && (
          <p className="friends__hint">No pending requests.</p>
        )}
        {!loading && incoming.length > 0 && (
          <ul className="friends__list">
            {incoming.map((request) => (
              <li className="friends__row" key={request.id}>
                <a className="friends__profile-link" href={`#/profile/${request.requester.id}`}>
                  <Avatar
                    src={request.requester.imageUrl}
                    name={fullName(request.requester)}
                    size={40}
                  />
                  <span>
                    <strong>{fullName(request.requester)}</strong>
                    <span>@{request.requester.username}</span>
                  </span>
                </a>
                <FriendActionButton
                  targetUserId={request.requester.id}
                  status="PENDING_INCOMING"
                  requestId={request.id}
                  onChange={() => void load()}
                />
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="panel friends__panel">
        <div className="panel__head">
          <h2 className="panel__title">Sent requests</h2>
        </div>
        {!loading && outgoing.length === 0 && (
          <p className="friends__hint">No outgoing requests.</p>
        )}
        {!loading && outgoing.length > 0 && (
          <ul className="friends__list">
            {outgoing.map((request) => (
              <li className="friends__row" key={request.id}>
                <a className="friends__profile-link" href={`#/profile/${request.addressee.id}`}>
                  <Avatar
                    src={request.addressee.imageUrl}
                    name={fullName(request.addressee)}
                    size={40}
                  />
                  <span>
                    <strong>{fullName(request.addressee)}</strong>
                    <span>@{request.addressee.username}</span>
                  </span>
                </a>
                <FriendActionButton
                  targetUserId={request.addressee.id}
                  status="PENDING_OUTGOING"
                  requestId={request.id}
                  onChange={() => void load()}
                />
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="panel friends__panel">
        <div className="panel__head">
          <h2 className="panel__title">Your friends</h2>
        </div>
        {!loading && friends.length === 0 && (
          <p className="friends__hint">No friends yet. Search above to connect.</p>
        )}
        {!loading && friends.length > 0 && (
          <ul className="friends__list">
            {friends.map((friend) => (
              <li className="friends__row" key={friend.requestId}>
                <a className="friends__profile-link" href={`#/profile/${friend.user.id}`}>
                  <Avatar src={friend.user.imageUrl} name={fullName(friend.user)} size={40} />
                  <span>
                    <strong>{fullName(friend.user)}</strong>
                    <span>@{friend.user.username}</span>
                  </span>
                </a>
                <FriendActionButton
                  targetUserId={friend.user.id}
                  status="FRIENDS"
                  requestId={friend.requestId}
                  onChange={() => void load()}
                />
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}
