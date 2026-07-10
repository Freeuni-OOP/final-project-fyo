import { useEffect, useState } from "react";
import { useAuth } from "../hooks/useAuth";
import { ApiError, teamsApi } from "./api";
import type { MyJoinRequest, TeamDetails } from "./types";
import { Button } from "./ui";

interface TeamJoinPanelProps {
  team: TeamDetails;
  /** Absent on the signed-out public view, which has to ask who is applying. */
  currentUserId?: number;
}

interface Problem {
  text: string;
  /** A 409 is not a failure to retry — it states a fact about the viewer. */
  kind: "warn" | "error";
}

/** `undefined` while the viewer's earlier request is still being looked up. */
type ExistingRequest = MyJoinRequest | null | undefined;

/**
 * Everything a viewer can do about their own membership of a team: join, see a
 * request they already sent, or learn why they can't. Shared by the drawer and
 * the full team page so the two can't drift apart.
 */
export function TeamJoinPanel({ team, currentUserId }: TeamJoinPanelProps) {
  const { getIdToken } = useAuth();
  const [existing, setExisting] = useState<ExistingRequest>(undefined);
  const [sentJustNow, setSentJustNow] = useState(false);

  const [formOpen, setFormOpen] = useState(false);
  const [typedUserId, setTypedUserId] = useState("");
  const [sending, setSending] = useState(false);
  const [problem, setProblem] = useState<Problem | null>(null);

  const isCaptain = currentUserId !== undefined && team.captain.id === currentUserId;
  const isMember =
    currentUserId !== undefined && team.members.some((m) => m.userId === currentUserId);
  const knownViewer = currentUserId !== undefined;

  // Reading the viewer's own requests lets us explain the situation up front,
  // instead of letting them discover it by being rejected.
  useEffect(() => {
    if (!knownViewer || isMember) return;
    let alive = true;
    (async () => {
      try {
        const token = await getIdToken();
        if (!token || !alive) return;
        const requests = await teamsApi.myJoinRequests(token);
        if (alive) setExisting(requests.find((r) => r.team.id === team.id) ?? null);
      } catch {
        if (alive) setExisting(null);
      }
    })();
    return () => {
      alive = false;
    };
  }, [knownViewer, currentUserId, isMember, team.id, getIdToken]);

  async function send(applicantId: number) {
    setSending(true);
    setProblem(null);
    try {
      const token = await getIdToken();
      if (!token) {
        setProblem({ text: "Your session expired. Sign in again.", kind: "error" });
        return;
      }
      await teamsApi.requestToJoin(token, team.id);
      setSentJustNow(true);
      setFormOpen(false);
      setTypedUserId("");
    } catch (err) {
      const error = err as ApiError;
      setProblem({ text: error.message, kind: error.status === 409 ? "warn" : "error" });
      // A conflict means our picture of the viewer is stale. Re-read it so the
      // panel settles on the real state rather than re-offering the button.
      if (error.status === 409 && knownViewer) {
        const token = await getIdToken();
        if (token) {
          teamsApi
            .myJoinRequests(token)
            .then((requests) => setExisting(requests.find((r) => r.team.id === team.id) ?? null))
            .catch(() => {});
        }
      }
    } finally {
      setSending(false);
    }
  }

  function submitTypedId(event: React.FormEvent) {
    event.preventDefault();
    const id = Number(typedUserId);
    if (!Number.isInteger(id) || id <= 0) {
      setProblem({ text: "Enter a valid numeric player id.", kind: "error" });
      return;
    }
    void send(id);
  }

  if (sentJustNow) {
    return (
      <p className="td__joined">
        ✓ Request sent! The captain will review it. <MyTeamsLink shown={knownViewer} />
      </p>
    );
  }

  if (isCaptain) {
    return <p className="td__closed-note">You are the captain of this team.</p>;
  }

  if (isMember) {
    return (
      <p className="td__joined">
        ✓ You already play for this team. <MyTeamsLink shown />
      </p>
    );
  }

  if (knownViewer && existing === undefined) {
    return <p className="td__closed-note">Checking your status…</p>;
  }

  if (existing?.status === "PENDING") {
    return (
      <p className="td__joined">
        ✓ Your request is pending — the captain has not answered yet.{" "}
        <MyTeamsLink shown />
      </p>
    );
  }

  if (existing?.status === "DECLINED") {
    return (
      <p className="td__notice td__notice--warn">
        This team declined your request to join.
      </p>
    );
  }

  if (!team.isRecruiting) {
    return <p className="td__closed-note">This team isn't taking new players right now.</p>;
  }

  if (team.openSpots <= 0) {
    return <p className="td__closed-note">Every spot on this roster is taken.</p>;
  }

  return (
    <>
      {knownViewer ? (
        <Button variant="solid" disabled={sending} onClick={() => void send(currentUserId)}>
          {sending ? "Sending…" : "Request to join →"}
        </Button>
      ) : formOpen ? (
        <form className="joinform" onSubmit={submitTypedId}>
          <label className="joinform__label" htmlFor="join-user">
            Your player id
          </label>
          <div className="joinform__row">
            <input
              id="join-user"
              className="joinform__input"
              inputMode="numeric"
              placeholder="e.g. 4"
              value={typedUserId}
              onChange={(e) => setTypedUserId(e.target.value)}
              autoFocus
            />
            <Button variant="optic" type="submit" disabled={sending}>
              {sending ? "Sending…" : "Send request"}
            </Button>
          </div>
        </form>
      ) : (
        <Button variant="solid" onClick={() => setFormOpen(true)}>
          Request to join →
        </Button>
      )}

      {problem && (
        <p className={`td__notice td__notice--${problem.kind}`}>{problem.text}</p>
      )}
    </>
  );
}

/** Only ever rendered for a signed-in viewer — `#/app` is behind auth. */
function MyTeamsLink({ shown }: { shown: boolean }) {
  if (!shown) return null;
  return (
    <a className="td__notice-link" href="#/app/my-teams">
      Your teams →
    </a>
  );
}
