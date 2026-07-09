import { Button, Wordmark } from "../teams/ui";
import { useSession } from "../session/SessionContext";
import "../teams/theme.css";
import "../teams/teams.css";
import "./AppShell.css";

/** Shown when we hold a Firebase session but cannot load the account behind it.
 *  Recoverable, so offer a retry before making the user sign in again. */
export function SessionError() {
  const { error, retry, signOut } = useSession();

  return (
    <div className="court splash">
      <Wordmark />
      <p className="splash__title">Can't load your account</p>
      <p className="splash__message">{error ?? "Something went wrong."}</p>
      <div className="splash__actions">
        <Button variant="solid" onClick={retry}>
          Try again
        </Button>
        <Button variant="ghost" onClick={signOut}>
          Log out
        </Button>
      </div>
    </div>
  );
}
