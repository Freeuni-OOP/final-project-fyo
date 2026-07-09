import { useState } from "react";
import { TeamsBoard } from "../../teams/TeamsBoard";
import { useTeams } from "../../teams/useTeams";
import { Button } from "../../teams/ui";
import { useSession } from "../../session/SessionContext";
import { PageHead } from "../AppShell";
import { CreateTeamDrawer } from "../CreateTeamDrawer";

export function TeamsPage() {
  const { user } = useSession();
  const { teams, loading, error, reload, applyRosterChange, addTeam } = useTeams();
  const [creating, setCreating] = useState(false);

  return (
    <>
      <PageHead
        eyebrow="Teams · Tbilisi & beyond"
        title="Find a squad"
        actions={
          user && (
            <Button variant="optic" onClick={() => setCreating(true)}>
              New team
            </Button>
          )
        }
      />

      <TeamsBoard
        teams={teams}
        loading={loading}
        error={error}
        onRetry={reload}
        onRosterChange={applyRosterChange}
        currentUserId={user?.id}
      />

      {creating && user && (
        <CreateTeamDrawer
          captainUserId={user.id}
          onClose={() => setCreating(false)}
          onCreated={addTeam}
        />
      )}
    </>
  );
}
