import { PlayersBoard } from "../../players/PlayersBoard";
import { usePlayers } from "../../players/usePlayers";
import { PageHead } from "../AppShell";

export function PlayersPage() {
  const { rows, loading, error, reload } = usePlayers();

  return (
    <>
      <PageHead eyebrow="Players · Tbilisi & beyond" title="Find a player" />

      <PlayersBoard rows={rows} loading={loading} error={error} onRetry={reload} />
    </>
  );
}
