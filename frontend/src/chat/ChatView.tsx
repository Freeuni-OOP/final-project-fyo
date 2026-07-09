import { useEffect, useMemo, useRef, useState } from "react";
import { ApiError, chatApi, socketUrl } from "./api";
import { MiniStompClient, type SocketStatus } from "./stomp";
import type { ChatMessage, Conversation } from "./types";
import { Avatar, Button, Wordmark } from "../teams/ui";
import "../teams/theme.css";
import "./chat.css";

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
  return others.map((p) => p.fullName).join(", ");
}

export function ChatView() {
  const [userInput, setUserInput] = useState("1");
  const [userId, setUserId] = useState<number>(1);
  const [peerInput, setPeerInput] = useState("2");

  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [activeId, setActiveId] = useState<number | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState("");

  const [loadingConversations, setLoadingConversations] = useState(false);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [socketStatus, setSocketStatus] = useState<SocketStatus>("idle");

  const socketRef = useRef<MiniStompClient | null>(null);
  const scrollRef = useRef<HTMLDivElement | null>(null);

  const activeConversation = useMemo(
    () => conversations.find((c) => c.id === activeId) ?? null,
    [activeId, conversations]
  );

  function setApiError(err: unknown) {
    setError(err instanceof ApiError || err instanceof Error ? err.message : "Something went wrong");
  }

  async function loadConversations(nextUserId = userId) {
    setLoadingConversations(true);
    setError(null);
    try {
      const loaded = await chatApi.conversations(nextUserId);
      setConversations(loaded);
      setActiveId((current) => current ?? loaded[0]?.id ?? null);
    } catch (err) {
      setApiError(err);
    } finally {
      setLoadingConversations(false);
    }
  }

  function applyUser() {
    const id = parseId(userInput);
    if (!id) {
      setError("Enter a valid numeric user id.");
      return;
    }
    setUserId(id);
    setActiveId(null);
    setMessages([]);
    void loadConversations(id);
  }

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
      const created = await chatApi.createDirect(userId, peerId);
      setConversations((prev) => {
        const exists = prev.some((c) => c.id === created.id);
        return exists ? prev.map((c) => (c.id === created.id ? created : c)) : [created, ...prev];
      });
      setActiveId(created.id);
    } catch (err) {
      setApiError(err);
    }
  }

  useEffect(() => {
    void loadConversations(1);
  }, []);

  useEffect(() => {
    if (activeId === null) {
      setMessages([]);
      return;
    }

    setLoadingMessages(true);
    setError(null);
    chatApi
      .messages(activeId, userId)
      .then(setMessages)
      .catch(setApiError)
      .finally(() => setLoadingMessages(false));
  }, [activeId, userId]);

  useEffect(() => {
    if (activeId === null) return;

    const client = new MiniStompClient(socketUrl(), setSocketStatus);
    socketRef.current = client;
    client.connect();

    const subId = client.subscribe(`/topic/conversations/${activeId}`, (body) => {
      try {
        const incoming = JSON.parse(body) as ChatMessage;
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
      client.unsubscribe(subId);
      client.disconnect();
      if (socketRef.current === client) socketRef.current = null;
    };
  }, [activeId]);

  useEffect(() => {
    scrollRef.current?.scrollTo({
      top: scrollRef.current.scrollHeight,
      behavior: "smooth",
    });
  }, [messages.length, activeId]);

  async function sendMessage(e: React.FormEvent) {
    e.preventDefault();
    if (activeId === null || !draft.trim()) return;

    const body = draft.trim();
    setDraft("");
    setSending(true);
    setError(null);

    try {
      if (socketStatus === "connected" && socketRef.current) {
        socketRef.current.publish(`/app/conversations/${activeId}/send`, {
          senderUserId: userId,
          body,
        });
      } else {
        const saved = await chatApi.send(activeId, userId, body);
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
          <p className="eyebrow">Match chat</p>
          <h1 className="section-title chat__title">Talk after the match</h1>
          <p className="section-lead">
            This temporary screen uses player ids until auth and real match acceptance are connected.
          </p>
        </section>

        <section className="chat__board" aria-label="Chat workspace">
          <aside className="chat__side">
            <div className="chat__panel-head">
              <div>
                <p className="chat__label">Current player</p>
                <strong>#{userId}</strong>
              </div>
              <span className={`chat__status chat__status--${socketStatus}`}>
                {socketStatus}
              </span>
            </div>

            <div className="chat__idrow">
              <input
                aria-label="Current user id"
                value={userInput}
                inputMode="numeric"
                onChange={(e) => setUserInput(e.target.value)}
              />
              <button type="button" onClick={applyUser}>
                Load
              </button>
            </div>

            <form className="chat__start" onSubmit={startConversation}>
              <label htmlFor="peer-id">Start with player id</label>
              <div className="chat__idrow">
                <input
                  id="peer-id"
                  value={peerInput}
                  inputMode="numeric"
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
                return (
                  <li key={conversation.id}>
                    <button
                      type="button"
                      className={conversation.id === activeId ? "chat__conversation chat__conversation--on" : "chat__conversation"}
                      onClick={() => setActiveId(conversation.id)}
                    >
                      <Avatar
                        src={others[0]?.imageUrl ?? null}
                        name={title}
                        size={38}
                      />
                      <span className="chat__conversation-copy">
                        <strong>{title}</strong>
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
                    <h2>{conversationName(activeConversation, userId)}</h2>
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

            {error && <p className="chat__error">{error}</p>}

            <div className="chat__messages" ref={scrollRef}>
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
