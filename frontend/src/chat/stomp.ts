type MessageHandler = (body: string) => void;
type StatusHandler = (status: SocketStatus) => void;

export type SocketStatus = "idle" | "connecting" | "connected" | "closed" | "error";

interface Subscription {
  destination: string;
  handler: MessageHandler;
}

export class MiniStompClient {
  private socket: WebSocket | null = null;
  private connected = false;
  private nextSubscriptionId = 1;
  private subscriptions = new Map<string, Subscription>();

  constructor(private url: string, private onStatus: StatusHandler) {}

  connect(connectHeaders: Record<string, string> = {}) {
    if (this.socket && this.socket.readyState <= WebSocket.OPEN) return;

    this.onStatus("connecting");
    this.socket = new WebSocket(this.url);

    this.socket.onopen = () => {
      this.sendFrame("CONNECT", {
        "accept-version": "1.2",
        "heart-beat": "0,0",
        ...connectHeaders,
      });
    };

    this.socket.onmessage = (event) => this.handleData(String(event.data));
    this.socket.onerror = () => this.onStatus("error");
    this.socket.onclose = () => {
      this.connected = false;
      this.onStatus("closed");
    };
  }

  disconnect() {
    if (!this.socket) return;
    if (this.connected) this.sendFrame("DISCONNECT", {});
    this.socket.close();
    this.socket = null;
    this.connected = false;
    this.subscriptions.clear();
    this.onStatus("closed");
  }

  subscribe(destination: string, handler: MessageHandler): string {
    const id = `sub-${this.nextSubscriptionId++}`;
    this.subscriptions.set(id, { destination, handler });
    if (this.connected) {
      this.sendFrame("SUBSCRIBE", { id, destination });
    }
    return id;
  }

  unsubscribe(id: string) {
    if (this.connected) this.sendFrame("UNSUBSCRIBE", { id });
    this.subscriptions.delete(id);
  }

  publish(destination: string, body: unknown) {
    this.sendFrame(
      "SEND",
      {
        destination,
        "content-type": "application/json",
      },
      JSON.stringify(body)
    );
  }

  private handleData(data: string) {
    for (const raw of data.split("\0")) {
      if (!raw.trim()) continue;
      const frame = parseFrame(raw);

      if (frame.command === "CONNECTED") {
        this.connected = true;
        this.onStatus("connected");
        this.subscriptions.forEach((sub, id) => {
          this.sendFrame("SUBSCRIBE", { id, destination: sub.destination });
        });
      }

      if (frame.command === "MESSAGE") {
        const id = frame.headers.subscription;
        const sub = id ? this.subscriptions.get(id) : undefined;
        sub?.handler(frame.body);
      }

      if (frame.command === "ERROR") {
        this.onStatus("error");
      }
    }
  }

  private sendFrame(command: string, headers: Record<string, string>, body = "") {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) return;
    const headerLines = Object.entries(headers).map(([key, value]) => `${key}:${value}`);
    this.socket.send(`${command}\n${headerLines.join("\n")}\n\n${body}\0`);
  }
}

function parseFrame(raw: string) {
  const [head, ...bodyParts] = raw.split("\n\n");
  const lines = head.split("\n").filter(Boolean);
  const command = lines.shift() ?? "";
  const headers: Record<string, string> = {};

  lines.forEach((line) => {
    const i = line.indexOf(":");
    if (i > -1) headers[line.slice(0, i)] = line.slice(i + 1);
  });

  return {
    command,
    headers,
    body: bodyParts.join("\n\n"),
  };
}
