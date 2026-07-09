import { useEffect, useState } from "react";
import { useAuth } from "../hooks/useAuth";
import { useReveal } from "../teams/useReveal";
import { Button, Wordmark } from "../teams/ui";
import "../teams/theme.css";
import "../teams/teams.css";
import { ProfileApiError, profileApi } from "./api";
import { ProfileView } from "./ProfileView";
import type { Profile } from "./types";
import "./profile.css";

const goHome = () => {
  window.location.hash = "#/";
};

function parseUserId(hash: string): number | null {
  // #/profile/123 or #/profile/123?...
  const match = hash.match(/^#\/profile\/(\d+)(?:[/?]|$)/);
  if (!match) return null;
  const id = Number(match[1]);
  return Number.isFinite(id) ? id : null;
}

export function PublicProfilePage() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const userId = parseUserId(window.location.hash);

  useReveal([profile, loading]);

  useEffect(() => {
    if (userId == null) {
      setError("Invalid profile link.");
      setLoading(false);
      return;
    }

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

  return (
    <div className="court pf-page">
      <header className="bar">
        <Wordmark onClick={goHome} />
        <nav className="bar__nav" aria-label="Primary">
          <a href="#/teams">Teams</a>
          {user && <a href="#/profile">Profile</a>}
        </nav>
        <Button variant="ghost" className="bar__cta" onClick={goHome}>
          ← Home
        </Button>
      </header>

      <main className="pf-shell">
        <div className="pf-toolbar">
          <p className="eyebrow">Public profile</p>
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
