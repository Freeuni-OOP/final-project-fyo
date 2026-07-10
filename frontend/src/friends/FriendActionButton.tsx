import { useState } from "react";
import { useAuth } from "../hooks/useAuth";
import { Button } from "../teams/ui";
import { FriendsApiError, friendsApi } from "./api";
import { openDirectChat } from "./openDirectChat";
import type { FriendRelationshipStatus } from "./types";

interface FriendActionButtonProps {
  targetUserId: number;
  status: FriendRelationshipStatus;
  requestId: number | null;
  onChange?: () => void;
}

export function FriendActionButton({
  targetUserId,
  status,
  requestId,
  onChange,
}: FriendActionButtonProps) {
  const { getIdToken } = useAuth();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function withToken(action: (token: string) => Promise<void>) {
    setBusy(true);
    setError(null);
    try {
      const token = await getIdToken();
      if (!token) throw new FriendsApiError(401, "Sign in to manage friends.");
      await action(token);
      onChange?.();
    } catch (err) {
      setError(err instanceof FriendsApiError ? err.message : "Something went wrong");
    } finally {
      setBusy(false);
    }
  }

  if (status === "FRIENDS") {
    return (
      <div className="friends__actions">
        <Button
          variant="optic"
          disabled={busy}
          onClick={() =>
            void withToken(async (token) => {
              await openDirectChat(token, targetUserId);
            })
          }
        >
          Message
        </Button>
        <Button
          variant="ghost"
          disabled={busy}
          onClick={() =>
            void withToken(async (token) => {
              await friendsApi.unfriend(token, targetUserId);
            })
          }
        >
          Unfriend
        </Button>
        {error && <p className="friends__error">{error}</p>}
      </div>
    );
  }

  if (status === "PENDING_OUTGOING" && requestId !== null) {
    return (
      <div className="friends__actions">
        <Button variant="ghost" disabled>
          Request sent
        </Button>
        <Button
          variant="ghost"
          disabled={busy}
          onClick={() =>
            void withToken(async (token) => {
              await friendsApi.cancel(token, requestId);
            })
          }
        >
          Cancel
        </Button>
        {error && <p className="friends__error">{error}</p>}
      </div>
    );
  }

  if (status === "PENDING_INCOMING" && requestId !== null) {
    return (
      <div className="friends__actions">
        <Button
          variant="optic"
          disabled={busy}
          onClick={() =>
            void withToken(async (token) => {
              await friendsApi.accept(token, requestId);
            })
          }
        >
          Accept
        </Button>
        <Button
          variant="ghost"
          disabled={busy}
          onClick={() =>
            void withToken(async (token) => {
              await friendsApi.decline(token, requestId);
            })
          }
        >
          Decline
        </Button>
        {error && <p className="friends__error">{error}</p>}
      </div>
    );
  }

  return (
    <div className="friends__actions">
      <Button
        variant="solid"
        disabled={busy}
        onClick={() =>
          void withToken(async (token) => {
            await friendsApi.sendRequest(token, targetUserId);
          })
        }
      >
        Add friend
      </Button>
      {error && <p className="friends__error">{error}</p>}
    </div>
  );
}
