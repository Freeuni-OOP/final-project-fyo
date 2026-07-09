import { LandingPage } from "./pages/LandingPage";
import { OnboardingPage } from "./pages/OnboardingPage";
import { ProfilePage } from "./profile/ProfilePage";
import { PublicProfilePage } from "./profile/PublicProfilePage";
import { TeamsView } from "./teams/TeamsView";
import { ChatView } from "./chat/ChatView";
import { AppShell } from "./app/AppShell";
import { Dashboard } from "./app/pages/Dashboard";
import { TeamsPage } from "./app/pages/TeamsPage";
import { Splash } from "./app/Splash";
import { SessionError } from "./app/SessionError";
import { isRoot, matchesRoute, Redirect, useHashRoute } from "./routing";
import { useSession } from "./session/SessionContext";
import Login from "./Login";
import Signup from "./Signup";

/**
 * Hash routes:
 *   public   #/  #/home  #/login  #/signup  #/teams
 *   signed in  #/app  #/app/teams  #/onboarding  #/chat
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

  if (matchesRoute(hash, "#/chat")) {
    return authed ? <ChatView /> : <Redirect to="#/login" />;
  }

  if (matchesRoute(hash, "#/login")) {
    return authed ? <Redirect to="#/app" /> : <Login />;
  }

  if (matchesRoute(hash, "#/signup")) {
    return authed ? <Redirect to="#/app" /> : <Signup />;
  }

  if (matchesRoute(hash, "#/onboarding")) {
    return authed ? <OnboardingPage /> : <Redirect to="#/login" />;
  }

  if (matchesRoute(hash, "#/app")) {
    return authed ? <AppRoutes hash={hash} /> : <Redirect to="#/login" />;
  }

  if (matchesRoute(hash, "#/teams")) {
    return authed ? <Redirect to="#/app/teams" /> : <TeamsView />;
  }

  // The landing page, still reachable while signed in.
  if (matchesRoute(hash, "#/home")) return <LandingPage />;

  if (isRoot(hash) && authed) return <Redirect to="#/app" />;

  return <LandingPage />;
}

function AppRoutes({ hash }: { hash: string }) {
  if (matchesRoute(hash, "#/app/teams")) {
    return (
      <AppShell>
        <TeamsPage />
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
