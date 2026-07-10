import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { ApiError, chatApi, socketUrl } from "./api";
import { MiniStompClient, type SocketStatus } from "./stomp";
import type { ChatMessage, Conversation } from "./types";
import { chatConversationPath, parseChatRoute } from "./routes";
import { useSession } from "../session/SessionContext";
import { useAuth } from "../hooks/useAuth";
import { useHashRoute } from "../routing";
import { Avatar, Button, Wordmark } from "../teams/ui";
import "../teams/theme.css";
import "./chat.css";

const MESSAGE_PAGE_SIZE = 50;

const goHome = () => {
  window.location.hash = "#/";
};

const goTeams = () => {
  window.location.hash = "#/teams";
};

function parseId(value: string): number | null {
  const id = Number(value);
  return Number.isInteger(id) && id > 0 ? id : null;
}

function formatTime(iso: string): string {
  try {
    return new Date(iso).toLocaleTimeString("en-US", {
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return iso;
  }
}

function otherParticipants(conversation: Conversation, userId: number) {
  return conversation.participants.filter((p) => p.userId !== userId);
}

function conversationName(conversation: Conversation, userId: number) {
  const others = otherParticipants(conversation, userId);
  if (others.length === 0) return "Just you";
  if (conversation.type === "TEAM") return `Team chat (${conversation.participants.length})`;
  if (others.length > 2) return `${others[0].fullName} +${others.length - 1}`;
  return others.map((p) => p.fullName).join(", ");
}

function conversationBadge(conversation: Conversation): string {
  if (conversation.type === "MATCH") return "Match";
  if (conversation.type === "TEAM") return "Team";
  return "Direct";
}

function friendlyApiError(err: unknown): string {
  if (!(err instanceof ApiError)) {
    return err instanceof Error ? err.message : "Something went wrong";
  }
  if (err.status === 403) return "You don't have access to this conversation.";
  if (err.status === 404) return "That conversation could not be found.";
  if (err.status === 401) return "Your session expired. Sign in again.";
  if (err.status === 0) return err.message;
  return err.message;
}

export function ChatView() {
  const hash = useHashRoute();
  const route = useMemo(() => parseChatRoute(hash), [hash]);
  const { user } = useSession();
  const { getIdToken } = useAuth();

  const userId = user?.id ?? null;

  const [peerInput, setPeerInput] = useState("");

  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [activeId, setActiveId] = useState<number | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [hasMoreMessages, setHasMoreMessages] = useState(false);
  const [draft, setDraft] = useState("");

  const [loadingConversations, setLoadingConversations] = useState(false);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [loadingOlder, setLoadingOlder] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [socketStatus, setSocketStatus] = useState<SocketStatus>("idle");

  const socketRef = useRef<MiniStompClient | null>(null);
  const scrollRef = useRef<HTMLDivElement | null>(null);
  const stickToBottomRef = useRef(true);

  const activeConversation = useMemo(
    () => conversations.find((c) => c.id === activeId) ?? null,
    [activeId, conversations]
  );

  function setApiError(err: unknown) {
    setError(friendlyApiError(err));
  }

  const requireToken = useCallback(async (): Promise<string> => {
    const token = await getIdToken();
    if (!token) {
      throw new ApiError(401, "Your session expired. Sign in again.");
    }
    return token;
  }, [getIdToken]);

  const selectConversation = useCallback((conversationId: number) => {
    setActiveId(conversationId);
    const target = chatConversationPath(conversationId);
    if (window.location.hash !== target) {
      window.location.hash = target;
    }
  }, []);

  const mergeConversation = useCallback((conversation: Conversation) => {
    setConversations((prev) => {
      const exists = prev.some((c) => c.id === conversation.id);
      return exists
        ? prev.map((c) => (c.id === conversation.id ? conversation : c))
        : [conversation, ...prev];
    });
  }, []);

  const loadConversations = useCallback(async () => {
    setLoadingConversations(true);
    setError(null);
    try {
      const token = await requireToken();
      const loaded = await chatApi.conversations(token);
      setConversations(loaded);
      if (route.conversationId !== null) {
        setActiveId(route.conversationId);
      } else if (route.matchId === null) {
        setActiveId((current) => current ?? loaded[0]?.id ?? null);
      }
    } catch (err) {
      setApiError(err);
    } finally {
      setLoadingConversations(false);
    }
  }, [requireToken, route.conversationId, route.matchId]);

  const loadOlderMessages = useCallback(async () => {
    if (activeId === null || loadingOlder || !hasMoreMessages || messages.length === 0) return;

    const container = scrollRef.current;
    const previousHeight = container?.scrollHeight ?? 0;
    setLoadingOlder(true);
    setError(null);

    try {
      const token = await requireToken();
      const older = await chatApi.messages(token, activeId, {
        before: messages[0].id,
        limit: MESSAGE_PAGE_SIZE,
      });
      stickToBottomRef.current = false;
      setMessages((prev) => {
        const seen = new Set(prev.map((m) => m.id));
        return [...older.filter((m) => !seen.has(m.id)), ...prev];
      });
      setHasMoreMessages(older.length === MESSAGE_PAGE_SIZE);
      requestAnimationFrame(() => {
        if (container) {
          container.scrollTop = container.scrollHeight - previousHeight;
        }
      });
    } catch (err) {
      setApiError(err);
    } finally {
      setLoadingOlder(false);
    }
  }, [activeId, hasMoreMessages, loadingOlder, messages, requireToken]);

  async function startConversation(e: React.FormEvent) {
    e.preventDefault();
    const peerId = parseId(peerInput);
    if (!peerId) {
      setError("Enter a valid numeric player id.");
      return;
    }
    if (peerId === userId) {
      setError("Choose a different player id.");
      return;
    }

    setError(null);
    try {
      const token = await requireToken();
      const created = await chatApi.createDirect(token, peerId);
      mergeConversation(created);
      selectConversation(created.id);
      setPeerInput("");
    } catch (err) {
      setApiError(err);
    }
  }

  useEffect(() => {
    if (userId !== null) void loadConversations();
  }, [userId, loadConversations]);

  useEffect(() => {
    if (route.conversationId !== null) {
      setActiveId(route.conversationId);
    }
  }, [route.conversationId]);

  useEffect(() => {
    if (route.matchId === null || userId === null) return;

    let alive = true;
    setError(null);
    (async () => {
      try {
        const token = await requireToken();
        const conversation = await chatApi.byMatch(token, route.matchId!);
        if (!alive) return;
        mergeConversation(conversation);
        selectConversation(conversation.id);
      } catch (err) {
        if (alive) setApiError(err);
      }
    })();

    return () => {
      alive = false;
    };
  }, [mergeConversation, requireToken, route.matchId, selectConversation, userId]);

  useEffect(() => {
    if (activeId === null) {
      setMessages([]);
      setHasMoreMessages(false);
      return;
    }

    let alive = true;
    stickToBottomRef.current = true;
    setLoadingMessages(true);
    setError(null);
    (async () => {
      try {
        const token = await requireToken();
        const loaded = await chatApi.messages(token, activeId, { limit: MESSAGE_PAGE_SIZE });
        if (!alive) return;
        setMessages(loaded);
        setHasMoreMessages(loaded.length === MESSAGE_PAGE_SIZE);
      } catch (err) {
        if (alive) setApiError(err);
      } finally {
        if (alive) setLoadingMessages(false);
      }
    })();

    return () => {
      alive = false;
    };
  }, [activeId, requireToken]);

  useEffect(() => {
    if (activeId === null) return;

    let alive = true;
    const client = new MiniStompClient(socketUrl(), setSocketStatus);
    socketRef.current = client;

    (async () => {
      try {
        const token = await requireToken();
        if (!alive) return;
        client.connect({ Authorization: `Bearer ${token}` });
      } catch (err) {
        if (alive) setApiError(err);
      }
    })();

    const subId = client.subscribe(`/topic/conversations/${activeId}`, (body) => {
      try {
        const incoming = JSON.parse(body) as ChatMessage;
        stickToBottomRef.current = true;
        setMessages((prev) => {
          if (prev.some((m) => m.id === incoming.id)) return prev;
          return [...prev, incoming];
        });
        setConversations((prev) =>
          prev.map((conversation) =>
            conversation.id === incoming.conversationId
              ? { ...conversation, lastMessage: incoming }
              : conversation
          )
        );
      } catch {
        setSocketStatus("error");
      }
    });

    return () => {
      alive = false;
      client.unsubscribe(subId);
      client.disconnect();
      if (socketRef.current === client) socketRef.current = null;
    };
  }, [activeId, requireToken]);

  useEffect(() => {
    if (!stickToBottomRef.current) return;
    scrollRef.current?.scrollTo({
      top: scrollRef.current.scrollHeight,
      behavior: "smooth",
    });
  }, [messages.length, activeId]);

  function onMessagesScroll() {
    const container = scrollRef.current;
    if (!container) return;

    stickToBottomRef.current =
      container.scrollHeight - container.scrollTop - container.clientHeight < 80;

    if (container.scrollTop < 48 && hasMoreMessages && !loadingOlder && !loadingMessages) {
      void loadOlderMessages();
    }
  }

  async function sendMessage(e: React.FormEvent) {
    e.preventDefault();
    if (activeId === null || userId === null || !draft.trim()) return;

    const body = draft.trim();
    setDraft("");
    setSending(true);
    setError(null);

    try {
      if (socketStatus === "connected" && socketRef.current) {
        socketRef.current.publish(`/app/conversations/${activeId}/send`, { body });
      } else {
        const token = await requireToken();
        const saved = await chatApi.send(token, activeId, body);
        stickToBottomRef.current = true;
        setMessages((prev) => [...prev, saved]);
        setConversations((prev) =>
          prev.map((conversation) =>
            conversation.id === saved.conversationId
              ? { ...conversation, lastMessage: saved }
              : conversation
          )
        );
      }
    } catch (err) {
      setDraft(body);
      setApiError(err);
    } finally {
      setSending(false);
    }
  }

  if (userId === null || !user) {
    return (
      <div className="court chat">
        <main className="chat__shell" id="chat">
          <p className="chat__state">
            You need to be signed in to use chat. <a href="#/login">Log in</a>
          </p>
        </main>
      </div>
    );
  }

  return (
    <div className="court chat">
      <header className="bar">
        <Wordmark onClick={goHome} />
        <nav className="bar__nav" aria-label="Primary">
          <button type="button" onClick={goTeams}>Teams</button>
          <a href="#/chat">Chat</a>
        </nav>
        <Button variant="ghost" className="bar__cta" onClick={goHome}>
          Home
        </Button>
      </header>

      <main className="chat__shell" id="chat">
        <section className="chat__intro">
          <p className="eyebrow">Messages</p>
          <h1 className="section-title chat__title">Match, team, and direct chat</h1>
          <p className="section-lead">
            Coordinate matches, talk with your squad, or message players directly.
          </p>
        </section>

        <section className="chat__board" aria-label="Chat workspace">
          <aside className="chat__side">
            <div className="chat__panel-head">
              <div>
                <p className="chat__label">Signed in as</p>
                <strong>@{user.username || user.name}</strong>
              </div>
              <span className={`chat__status chat__status--${socketStatus}`}>
                {socketStatus}
              </span>
            </div>

            <form className="chat__start" onSubmit={startConversation}>
              <label htmlFor="peer-id">Start with player id</label>
              <div className="chat__idrow">
                <input
                  id="peer-id"
                  value={peerInput}
                  inputMode="numeric"
                  placeholder="e.g. 2"
                  onChange={(e) => setPeerInput(e.target.value)}
                />
                <button type="submit">Start</button>
              </div>
            </form>

            <div className="chat__list-head">
              <span>Conversations</span>
              <button type="button" onClick={() => loadConversations()} disabled={loadingConversations}>
                Refresh
              </button>
            </div>

            {loadingConversations && <p className="chat__state">Loading conversations...</p>}

            {!loadingConversations && conversations.length === 0 && (
              <p className="chat__state">No conversations yet.</p>
            )}

            <ul className="chat__conversations">
              {conversations.map((conversation) => {
                const others = otherParticipants(conversation, userId);
                const title = conversationName(conversation, userId);
                const badge = conversationBadge(conversation);
                return (
                  <li key={conversation.id}>
                    <button
                      type="button"
                      className={conversation.id === activeId ? "chat__conversation chat__conversation--on" : "chat__conversation"}
                      onClick={() => selectConversation(conversation.id)}
                    >
                      <Avatar
                        src={others[0]?.imageUrl ?? null}
                        name={title}
                        size={38}
                      />
                      <span className="chat__conversation-copy">
                        <strong>
                          {title}
                          <span className={`chat__pill chat__pill--${conversation.type.toLowerCase()}`}>
                            {badge}
                          </span>
                        </strong>
                        <span>
                          {conversation.lastMessage?.body ?? `Conversation #${conversation.id}`}
                        </span>
                      </span>
                    </button>
                  </li>
                );
              })}
            </ul>
          </aside>

          <section className="chat__main">
            <header className="chat__thread-head">
              {activeConversation ? (
                <>
                  <div>
                    <p className="chat__label">Conversation</p>
                    <h2>
                      {conversationName(activeConversation, userId)}
                      <span className={`chat__pill chat__pill--${activeConversation.type.toLowerCase()}`}>
                        {conversationBadge(activeConversation)}
                      </span>
                    </h2>
                  </div>
                  <span>#{activeConversation.id}</span>
                </>
              ) : (
                <div>
                  <p className="chat__label">Conversation</p>
                  <h2>Select a chat</h2>
                </div>
              )}
            </header>

            {socketStatus !== "connected" && activeConversation && (
              <p className="chat__socket-banner" role="status">
                Live updates are offline ({socketStatus}). New messages still send through the server.
              </p>
            )}

            {error && <p className="chat__error">{error}</p>}

            <div className="chat__messages" ref={scrollRef} onScroll={onMessagesScroll}>
              {loadingOlder && <p className="chat__state">Loading older messages...</p>}

              {!loadingOlder && hasMoreMessages && messages.length > 0 && (
                <button type="button" className="chat__load-more" onClick={() => void loadOlderMessages()}>
                  Load older messages
                </button>
              )}

              {loadingMessages && <p className="chat__state">Loading messages...</p>}

              {!loadingMessages && activeConversation && messages.length === 0 && (
                <p className="chat__state">No messages yet.</p>
              )}

              {!activeConversation && <p className="chat__state">Choose or start a conversation.</p>}

              {messages.map((message) => {
                const mine = message.senderId === userId;
                return (
                  <article
                    className={mine ? "chat__message chat__message--mine" : "chat__message"}
                    key={message.id}
                  >
                    <p>{message.body}</p>
                    <footer>
                      <span>@{message.senderUsername}</span>
                      <time dateTime={message.createdAt}>{formatTime(message.createdAt)}</time>
                    </footer>
                  </article>
                );
              })}
            </div>

            <form className="chat__composer" onSubmit={sendMessage}>
              <input
                value={draft}
                onChange={(e) => setDraft(e.target.value)}
                placeholder={activeConversation ? "Write a message" : "Select a conversation first"}
                disabled={!activeConversation || sending}
              />
              <button type="submit" disabled={!activeConversation || sending || !draft.trim()}>
                Send
              </button>
            </form>
          </section>
        </section>
      </main>
    </div>
  );
}
