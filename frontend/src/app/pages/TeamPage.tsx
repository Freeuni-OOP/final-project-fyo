import { TeamDetailPage } from "../../teams/TeamDetailPage";
import { useSession } from "../../session/SessionContext";

/** `#/app/teams/:id` — the team page inside the platform shell. */
export function TeamPage({ teamId }: { teamId: number }) {
  const { user } = useSession();
  return (
    <TeamDetailPage teamId={teamId} backHref="#/app/teams" currentUserId={user?.id} />
  );
}
