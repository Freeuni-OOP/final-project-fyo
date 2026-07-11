import { useEffect, useState } from "react";
import { useAuth } from "../hooks/useAuth";
import { useSession } from "../session/SessionContext";

const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8081";

export type NotificationBucket = "#/app/chat" | "#/app/friends" | "#/app/matches";

type NotificationCounts = Partial<Record<NotificationBucket, number>>;

interface NotificationEventPayload {
  id: number;
  recipientUserId: number;
  type: string;
  message: string;
  link: string | null;
  createdAt: string;
}

function bucketFor(payload: NotificationEventPayload): NotificationBucket | null {
  if (payload.link?.startsWith("#/app/chat") || payload.type === "MESSAGE") return "#/app/chat";
  if (payload.link?.startsWith("#/app/friends") || payload.type.startsWith("FRIEND_")) return "#/app/friends";
  if (payload.link?.startsWith("#/app/matches") || payload.type.startsWith("MATCH_") || payload.type === "NEW_MATCH_LISTING") {
    return "#/app/matches";
  }
  return null;
}

export function useNotificationCount() {
  const { status, user } = useSession();
  const { getIdToken } = useAuth();
  const [counts, setCounts] = useState<NotificationCounts>({});
  const [latestByBucket, setLatestByBucket] = useState<Partial<Record<NotificationBucket, NotificationEventPayload>>>({});

  useEffect(() => {
    if (status !== "authed" || !user) {
      setCounts({});
      setLatestByBucket({});
      return;
    }

    let source: EventSource | null = null;
    let cancelled = false;

    (async () => {
      const token = await getIdToken();
      if (!token || cancelled) return;

      const url = new URL(`${BASE}/api/notifications/stream`);
      url.searchParams.set("token", token);
      source = new EventSource(url.toString());

      source.addEventListener("notification", (event) => {
        try {
          const payload = JSON.parse(event.data) as NotificationEventPayload;
          if (payload.type === "CONNECTED") return;

          const bucket = bucketFor(payload);
          if (!bucket) return;

          setLatestByBucket((current) => ({ ...current, [bucket]: payload }));
          setCounts((current) => ({ ...current, [bucket]: (current[bucket] ?? 0) + 1 }));
        } catch {
          /* Ignore malformed SSE payloads. */
        }
      });
    })();

    return () => {
      cancelled = true;
      source?.close();
    };
  }, [getIdToken, status, user]);

  return {
    counts,
    latestByBucket,
    clear: (bucket: NotificationBucket) => {
      setCounts((current) => ({ ...current, [bucket]: 0 }));
    },
  };
}
