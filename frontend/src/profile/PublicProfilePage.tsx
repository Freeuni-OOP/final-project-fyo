import { useCallback, useEffect, useState } from "react";
import { useAuth } from "../hooks/useAuth";
import { useSession } from "../session/SessionContext";
import { useReveal } from "../teams/useReveal";
import { Button, Wordmark } from "../teams/ui";
import "../teams/theme.css";
import "../teams/teams.css";
import { FriendActionButton } from "../friends/FriendActionButton";
import { FriendsApiError, friendsApi } from "../friends/api";
import type { FriendRelationshipStatus } from "../friends/types";
import { ProfileApiError, profileApi } from "./api";
import { ProfileView } from "./ProfileView";
import type { Profile } from "./types";
import "../friends/friends.css";
import "./profile.css";

const goHome = () => {
  window.location.hash = "#/";
};

interface PublicProfilePageProps {
  userId: number;
}

export function PublicProfilePage({ userId }: PublicProfilePageProps) {
  const { user: sessionUser } = useSession();
  const { getIdToken } = useAuth();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [friendStatus, setFriendStatus] = useState<FriendRelationshipStatus>("NONE");
  const [friendRequestId, setFriendRequestId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const isSelf = sessionUser?.id === userId;

  const loadFriendStatus = useCallback(async () => {
    if (!sessionUser || isSelf) return;
    try {
      const token = await getIdToken();
      if (!token) return;
      const status = await friendsApi.status(token, userId);
      setFriendStatus(status.status);
      setFriendRequestId(status.requestId);
    } catch {
      /* profile still renders without friend actions */
    }
  }, [getIdToken, isSelf, sessionUser, userId]);

  useReveal([profile, loading]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    profileApi
      .getPublic(userId)
      .then((data) => {
        if (!cancelled) setProfile(data);
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          setError(
            err instanceof ProfileApiError
              ? err.message
              : err instanceof Error
                ? err.message
                : "Failed to load profile"
          );
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [userId]);

  useEffect(() => {
    void loadFriendStatus();
  }, [loadFriendStatus]);

  return (
    <div className="court pf-page">
      <header className="bar">
        <Wordmark onClick={goHome} />
        <nav className="bar__nav" aria-label="Primary">
          <a href="#/app/teams">Teams</a>
          <a href="#/app/friends">Friends</a>
          {sessionUser && <a href="#/app/profile">Profile</a>}
        </nav>
        <Button variant="ghost" className="bar__cta" onClick={goHome}>
          ← Home
        </Button>
      </header>

      <main className="pf-shell">
        <div className="pf-toolbar">
          <p className="eyebrow">Public profile</p>
          {!isSelf && sessionUser && (
            <FriendActionButton
              targetUserId={userId}
              status={friendStatus}
              requestId={friendRequestId}
              onChange={() => void loadFriendStatus()}
            />
          )}
          {isSelf && (
            <a className="panel__link" href="#/app/profile">
              Edit your profile →
            </a>
          )}
        </div>

        {loading && <p className="pf-status">Loading profile…</p>}
        {error && (
          <p className="pf-error" role="alert">
            {error}
          </p>
        )}
        {profile && <ProfileView profile={profile} />}
      </main>
    </div>
  );
}
