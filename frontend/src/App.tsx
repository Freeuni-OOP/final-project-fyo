import { LandingPage } from "./pages/LandingPage";
import { OnboardingPage } from "./pages/OnboardingPage";
import { ProfilePage } from "./profile/ProfilePage";
import { PublicProfilePage } from "./profile/PublicProfilePage";
import { PublicTeamPage } from "./teams/PublicTeamPage";
import { TeamsView } from "./teams/TeamsView";
import { ChatView } from "./chat/ChatView";
import { AppShell } from "./app/AppShell";
import { Dashboard } from "./app/pages/Dashboard";
import { MyTeamsPage } from "./app/pages/MyTeamsPage";
import { TeamPage } from "./app/pages/TeamPage";
import { TeamsPage } from "./app/pages/TeamsPage";
import { Splash } from "./app/Splash";
import { SessionError } from "./app/SessionError";
import { isRoot, matchesRoute, Redirect, routeId, routeParam, useHashRoute } from "./routing";
import { useSession } from "./session/SessionContext";
import { AdminPage } from "./app/pages/AdminPage";
import { FriendsPage } from "./friends/FriendsPage";
import { MatchesPage } from "./app/pages/MatchesPage";
import Login from "./Login";
import Signup from "./Signup";


/**
 * Hash routes:
 *   public   #/  #/home  #/login  #/signup  #/teams  #/teams/:id
 *   signed in  #/app  #/app/teams  #/app/teams/:id  #/app/my-teams  #/app/friends
 *              #/app/matches  #/app/profile  #/profile/:id  #/onboarding  #/chat
 *
 * Signed-in users are redirected off the public marketing pages and into the
 * platform shell. In-page anchors on the landing page (`#sports`, `#how`) are
 * not routes — they never start with `#/`, so they fall through to the landing.
 */
export default function App() {
  const hash = useHashRoute();
  const { status, user } = useSession();

  if (status === "loading") return <Splash />;

  // A Firebase session we couldn't back with an account. Don't route around it:
  // every page would misbehave, and the failure has to be visible somewhere.
  if (status === "error") return <SessionError />;

  const authed = status === "authed" && user !== null;

  // A half-set-up account can't use anything else.
  if (authed && user.onboarding && !matchesRoute(hash, "#/onboarding")) {
    return <Redirect to="#/onboarding" />;
  }

  if (matchesRoute(hash, "#/matches")) {
    if (authed) return <Redirect to="#/app/matches" />;
  }

  if (matchesRoute(hash, "#/chat")) {
    return authed ? <ChatView /> : <Redirect to="#/login" />;
  }

  if (matchesRoute(hash, "#/profile")) {
    const profileUserId = routeId(hash, "#/profile");
    if (profileUserId === null) {
      return authed ? <Redirect to="#/app/profile" /> : <Redirect to="#/login" />;
    }
    return authed ? <PublicProfilePage userId={profileUserId} /> : <Redirect to="#/login" />;
  }

  if (matchesRoute(hash, "#/login")) {
    if (!authed) return <Login />;
    return <Redirect to={user.onboarding ? "#/onboarding" : "#/app"} />;
  }

  if (matchesRoute(hash, "#/signup")) {
    if (!authed) return <Signup />;
    return <Redirect to={user.onboarding ? "#/onboarding" : "#/app"} />;
  }

  if (matchesRoute(hash, "#/onboarding")) {
    return authed ? <OnboardingPage /> : <Redirect to="#/login" />;
  }

  if (matchesRoute(hash, "#/app")) {
    return authed ? <AppRoutes hash={hash} /> : <Redirect to="#/login" />;
  }

  if (matchesRoute(hash, "#/teams")) {
    const segment = routeParam(hash, "#/teams");
    if (authed) {
      return <Redirect to={segment ? `#/app/teams/${segment}` : "#/app/teams"} />;
    }
    if (segment === null) return <TeamsView />;

    const teamId = routeId(hash, "#/teams");
    return teamId === null ? <Redirect to="#/teams" /> : <PublicTeamPage teamId={teamId} />;
  }

  // The landing page, still reachable while signed in.
  if (matchesRoute(hash, "#/home")) return <LandingPage />;

  if (isRoot(hash) && authed) return <Redirect to="#/app" />;

  return <LandingPage />;
}

function AppRoutes({ hash }: { hash: string }) {
  if (matchesRoute(hash, "#/app/my-teams")) {
    return (
      <AppShell>
        <MyTeamsPage />
      </AppShell>
    );
  }

  if (matchesRoute(hash, "#/app/teams")) {
    const segment = routeParam(hash, "#/app/teams");
    if (segment === null) {
      return (
        <AppShell>
          <TeamsPage />
        </AppShell>
      );
    }

    const teamId = routeId(hash, "#/app/teams");
    if (teamId === null) return <Redirect to="#/app/teams" />;
    return (
      <AppShell>
        <TeamPage teamId={teamId} />
      </AppShell>
    );
  }

  if (matchesRoute(hash, "#/app/admin")) {
    return (
      <AppShell>
        <AdminPage />
      </AppShell>
    );
  }

  if (matchesRoute(hash, "#/app/friends")) {
    return (
      <AppShell>
        <FriendsPage />
      </AppShell>
    );
  }

  if (matchesRoute(hash, "#/app/matches")) {
    return (
      <AppShell>
        <MatchesPage />
      </AppShell>
    );
  }

  if (matchesRoute(hash, "#/app/profile")) {
    return (
      <AppShell>
        <ProfilePage />
      </AppShell>
    );
  }

  if (hash === "#/app" || hash === "#/app/") {
    return (
      <AppShell>
        <Dashboard />
      </AppShell>
    );
  }

  return <Redirect to="#/app" />;
}
