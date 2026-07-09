import { useEffect, useState } from "react";
import { getSports, type SportDto } from "../api/Sports";
import { useAuth } from "../hooks/useAuth";
import { useReveal } from "../teams/useReveal";
import { Button, Wordmark } from "../teams/ui";
import "../teams/theme.css";
import "../teams/teams.css";
import { ProfileApiError, profileApi } from "./api";
import { ProfileEditForm } from "./ProfileEditForm";
import { ProfileView } from "./ProfileView";
import type { Profile, UpdateProfileRequest } from "./types";
import "./profile.css";

const goHome = () => {
  window.location.hash = "#/";
};

export function ProfilePage() {
  const { user, loading: authLoading, getIdToken, signOut } = useAuth();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [sports, setSports] = useState<SportDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [editing, setEditing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useReveal([profile, editing, loading]);

  useEffect(() => {
    if (authLoading) return;
    if (!user) {
      window.location.hash = "#/login";
      return;
    }

    let cancelled = false;
    setLoading(true);
    setError(null);

    (async () => {
      try {
        const token = await getIdToken();
        if (!token) {
          window.location.hash = "#/login";
          return;
        }
        const [me, sportList] = await Promise.all([
          profileApi.getMe(token),
          getSports(),
        ]);
        if (!cancelled) {
          setProfile(me);
          setSports(sportList);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "Failed to load profile");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [authLoading, user, getIdToken]);

  async function handleSave(payload: UpdateProfileRequest) {
    setSaving(true);
    setError(null);
    try {
      const token = await getIdToken();
      if (!token) {
        window.location.hash = "#/login";
        return;
      }
      const updated = await profileApi.updateMe(token, payload);
      setProfile(updated);
      setEditing(false);
    } catch (err) {
      setError(
        err instanceof ProfileApiError
          ? err.message
          : err instanceof Error
            ? err.message
            : "Failed to save profile"
      );
    } finally {
      setSaving(false);
    }
  }

  async function handleSignOut() {
    await signOut();
    window.location.hash = "#/";
  }

  return (
    <div className="court pf-page">
      <header className="bar">
        <Wordmark onClick={goHome} />
        <nav className="bar__nav" aria-label="Primary">
          <a href="#/teams">Teams</a>
          <a href="#/profile">Profile</a>
        </nav>
        <Button variant="ghost" className="bar__cta" onClick={goHome}>
          ← Home
        </Button>
      </header>

      <main className="pf-shell">
        <div className="pf-toolbar">
          <p className="eyebrow">Your profile</p>
          <div className="pf-toolbar__actions">
            {profile && !editing && (
              <Button variant="solid" onClick={() => setEditing(true)}>
                Edit profile
              </Button>
            )}
            <Button variant="ghost" onClick={handleSignOut}>
              Log out
            </Button>
          </div>
        </div>

        {(authLoading || loading) && <p className="pf-status">Loading profile…</p>}
        {error && !editing && (
          <p className="pf-error" role="alert">
            {error}
          </p>
        )}

        {profile && !editing && <ProfileView profile={profile} showEmail />}
        {profile && editing && (
          <ProfileEditForm
            profile={profile}
            sports={sports}
            submitting={saving}
            error={error}
            onCancel={() => {
              setEditing(false);
              setError(null);
            }}
            onSubmit={handleSave}
          />
        )}
      </main>
    </div>
  );
}
