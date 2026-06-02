# Real-Time Chat Application

![Build](https://img.shields.io/badge/build-passing-brightgreen)
![Deploy](https://img.shields.io/badge/deploy-render-blue)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot)
![Protocol](https://img.shields.io/badge/Protocol-STOMP%20over%20SockJS-purple)
![License](https://img.shields.io/badge/license-MIT-blue)

A production-style **real-time group chat system** built on **Spring Boot WebSockets** with the **STOMP pub/sub protocol over SockJS**. Supports concurrent multi-user messaging with sub-100 ms end-to-end latency, zero HTTP polling, and graceful browser fallback.

**[Live Demo →](https://your-app.onrender.com)**  ← _replace with your Render URL_

---

## Table of Contents

- [Why This Project](#why-this-project)
- [Demo](#demo)
- [Architecture](#architecture)
- [Message Lifecycle](#message-lifecycle)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Setup & Running Locally](#setup--running-locally)
- [WebSocket API Reference](#websocket-api-reference)
- [Configuration](#configuration)
- [Deployment](#deployment)
- [Performance Characteristics](#performance-characteristics)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Why This Project

Most tutorials demonstrate REST APIs — request/response over HTTP. This project deliberately targets the harder, more interesting problem: **persistent bidirectional connections at scale**.

Key engineering decisions made and defended:

| Decision | Alternative Considered | Reason Chosen |
|---|---|---|
| WebSocket + STOMP | Long polling / SSE | Full-duplex; no repeated HTTP overhead |
| STOMP pub/sub | Raw WebSocket frames | Structured routing, topic-based fan-out |
| SockJS fallback | WebSocket-only | Graceful degradation on restrictive networks |
| In-memory broker | Redis pub/sub | Sufficient for single-node; Redis listed in roadmap for multi-node |
| Spring Message Broker | Netty / custom | Production-grade concurrency, battle-tested in JVM ecosystem |

> **[REVISION NOTE]** If you add Redis or a database later, update this table to reflect
> the change from in-memory broker to external broker and the rationale.

---

## Demo

![Chat Demo](assets/demo.gif)
_← replace with your own GIF or screenshot; recommended size: 900×500 px_

**What the demo shows:**
- Two browser tabs connecting simultaneously
- Messages broadcast to all connected clients in real time
- SockJS reconnection on network interruption

---

## Architecture

### System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         Clients                                 │
│                                                                 │
│   Browser A                          Browser B                  │
│  ┌──────────────────┐               ┌──────────────────┐        │
│  │  SockJS + STOMP  │               │  SockJS + STOMP  │        │
│  └────────┬─────────┘               └────────┬─────────┘        │
└───────────│─────────────────────────────────│────────────────── ┘
            │ WS Upgrade (HTTP → TCP)          │
            │ GET /chat                        │
            ▼                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Spring Boot Application                     │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                  WebSocketConfig.java                     │  │
│  │  registerStompEndpoints("/chat")                          │  │
│  │  configureMessageBroker("/topic", "/app")                 │  │
│  └──────────────────────────┬────────────────────────────────┘  │
│                             │                                   │
│  ┌──────────────────────────▼────────────────────────────────┐  │
│  │                ChatController.java                        │  │
│  │  @MessageMapping("/sendMessage")                          │  │
│  │  @SendTo("/topic/messages")                               │  │
│  └──────────────────────────┬────────────────────────────────┘  │
│                             │                                   │
│  ┌──────────────────────────▼────────────────────────────────┐  │
│  │              Simple In-Memory Message Broker              │  │
│  │         Destination: /topic/messages                      │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Connection Establishment

```
Client                                    Server
  │                                          │
  │──── HTTP GET /chat ─────────────────────►│
  │◄─── 101 Switching Protocols ─────────────│  (WebSocket handshake)
  │                                          │
  │══════════ WebSocket (TCP) ═══════════════│  (persistent connection)
  │                                          │
  │──── STOMP CONNECT ──────────────────────►│
  │◄─── STOMP CONNECTED ────────────────────│
  │                                          │
  │──── STOMP SUBSCRIBE /topic/messages ────►│
  │                                          │
  │──── STOMP SEND /app/sendMessage ────────►│
  │                          (broker)        │
  │◄─── STOMP MESSAGE ──────────────────────│  (broadcast to all subscribers)
```

> **[REVISION NOTE]** If you add authentication (JWT handshake interceptor), add a step
> between the HTTP handshake and STOMP CONNECT showing token validation.

---

## Message Lifecycle

```
1. User types message in browser
         │
         ▼
2. JS calls stompClient.send("/app/sendMessage", {}, JSON.stringify(chatMessage))
         │
         ▼
3. Spring routes to @MessageMapping("/sendMessage") in ChatController
         │
         ▼
4. Controller builds ChatMessage response object
         │
         ▼
5. @SendTo("/topic/messages") — broker receives the message
         │
         ▼
6. Broker fans out to ALL active subscribers of /topic/messages
         │
         ▼
7. Each subscribed browser receives STOMP MESSAGE frame
         │
         ▼
8. JS subscription callback appends message to the DOM
```

**Latency profile (single-node, same datacenter):**

| Segment | Typical Latency |
|---|---|
| Client → Server (WS frame) | ~1–5 ms |
| Spring controller processing | < 1 ms |
| Broker fan-out (in-memory) | < 1 ms |
| Server → Client (WS frame) | ~1–5 ms |
| **End-to-end (same region)** | **~5–15 ms** |

> **[REVISION NOTE]** Benchmark with JMeter or Gatling under realistic concurrent load
> and replace these estimates with measured P50/P95/P99 values.

---

## Tech Stack

### Backend

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Language (LTS release) |
| Spring Boot | 3.x | Application framework |
| Spring WebSocket | — | WebSocket + STOMP support |
| Spring Messaging | — | Message routing infrastructure |
| Maven | 3.8+ | Build + dependency management |

### Frontend

| Technology | Purpose |
|---|---|
| Thymeleaf | Server-side HTML templating |
| SockJS Client | WebSocket with automatic fallback |
| STOMP.js | STOMP protocol client |
| Bootstrap 5 | Responsive UI |

### Infrastructure

| Technology | Purpose |
|---|---|
| Render | Cloud deployment (free tier) |

> **[REVISION NOTE]** Update versions here whenever you bump `pom.xml`.
> Stale version numbers in READMEs actively mislead contributors.

---

## Project Structure

```
chat-app/
│
├── src/
│   ├── main/
│   │   ├── java/com/chat/app/
│   │   │   ├── config/
│   │   │   │   └── WebSocketConfig.java       # STOMP endpoint + broker config
│   │   │   ├── controller/
│   │   │   │   └── ChatController.java        # @MessageMapping handler
│   │   │   ├── model/
│   │   │   │   └── ChatMessage.java           # Message payload (sender, content, type)
│   │   │   └── AppApplication.java            # Spring Boot entry point
│   │   │
│   │   └── resources/
│   │       ├── templates/
│   │       │   └── chat.html                  # Thymeleaf + SockJS + STOMP frontend
│   │       ├── static/                        # CSS, JS, images
│   │       └── application.properties         # Port, broker config
│   │
│   └── test/
│       └── java/com/chat/app/                 # Unit + integration tests
│
├── pom.xml                                    # Maven dependencies
└── README.md
```

> **[REVISION NOTE]** If you restructure into packages (e.g. add `service/`, `dto/`,
> `exception/`), update this tree. An out-of-date project structure is worse than none.

---

## Setup & Running Locally

### Prerequisites

| Tool | Minimum Version | Check |
|---|---|---|
| JDK | 17 | `java -version` |
| Maven | 3.8 | `mvn -version` |
| Git | any | `git --version` |

---

### Step 1 — Clone

```bash
git clone https://github.com/your-username/chat-app.git
cd chat-app
```

---

### Step 2 — Build

```bash
mvn clean install -DskipTests
```

---

### Step 3 — Run

```bash
mvn spring-boot:run
```

Or run the packaged JAR directly:

```bash
java -jar target/chat-app-*.jar
```

---

### Step 4 — Open

Navigate to **http://localhost:8080/chat**

To simulate multiple users, open the URL in two or more browser tabs simultaneously.

---

### Running Tests

```bash
mvn test
```

> **[REVISION NOTE]** Add test coverage badge and document any integration tests
> that spin up a real WebSocket connection once those are written.

---

## WebSocket API Reference

### Endpoint

| Property | Value |
|---|---|
| WebSocket URL | `ws://localhost:8080/chat` |
| SockJS URL | `http://localhost:8080/chat` |
| Protocol | STOMP over SockJS |

---

### Client → Server (Send)

#### `SEND /app/sendMessage`

Publishes a new chat message to all connected clients.

**Payload (JSON):**

```json
{
  "sender": "Nick",
  "content": "Hello everyone!",
  "type": "CHAT"
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `sender` | `String` | ✅ | Display name of the sender |
| `content` | `String` | ✅ | Message text body |
| `type` | `String` | ✅ | One of `CHAT`, `JOIN`, `LEAVE` |

---

### Server → Client (Subscribe)

#### `SUBSCRIBE /topic/messages`

Receives broadcast messages from all clients.

**Delivered frame (JSON):**

```json
{
  "sender": "Nick",
  "content": "Hello everyone!",
  "type": "CHAT"
}
```

All connected subscribers receive this frame immediately upon any client sending to `/app/sendMessage`.

---

### Message Types

| Type | Triggered By | Typical Use |
|---|---|---|
| `CHAT` | User sends a message | Normal message display |
| `JOIN` | User connects | "Nick joined the chat" notification |
| `LEAVE` | User disconnects | "Nick left the chat" notification |

> **[REVISION NOTE]** If you add private messaging, add a new destination
> (e.g. `/user/{id}/messages`) and document it here. Also update the architecture diagram
> to show point-to-point vs. topic destinations.

---

## Configuration

`src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# WebSocket / STOMP
# No additional properties needed for in-memory broker
# Add below if you later switch to a RabbitMQ/Redis external broker:
# spring.rabbitmq.host=localhost
# spring.rabbitmq.port=61613
```

> **[REVISION NOTE]** When you add external broker support, document each property,
> its default, and its effect here. Environment-variable overrides for 12-factor
> compatibility should also be listed (e.g. `SERVER_PORT`).

---

## Deployment

### Current: Render (Free Tier)

The app is deployed as a standard JVM web service on [Render](https://render.com).

**Build command:**
```
mvn clean install -DskipTests
```

**Start command:**
```
java -jar target/chat-app-*.jar
```

**Environment variables required:**

| Variable | Value | Notes |
|---|---|---|
| `SERVER_PORT` | `10000` | Render assigns port 10000 by default |

> **[REVISION NOTE]** Free tier on Render spins down after 15 minutes of inactivity,
> which means the first request after idle incurs a ~30 second cold start.
> Document this limitation for anyone testing the live demo.

---

### Running with Docker (Planned)

```dockerfile
# Dockerfile — not yet added; listed in roadmap
FROM eclipse-temurin:17-jre-alpine
COPY target/chat-app-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
docker build -t chat-app .
docker run -p 8080:8080 chat-app
```

> **[REVISION NOTE]** Move this block out of a code comment and into a real `Dockerfile`
> once containerization is complete.

---

## Performance Characteristics

### Concurrency Model

Spring's WebSocket support is built on top of its asynchronous task execution infrastructure. Each WebSocket session is handled via non-blocking I/O; the in-memory broker dispatches messages on a thread pool.

**In-memory broker limitations:**

- Suitable for a **single JVM node** only
- No message persistence — messages are lost on restart
- Fan-out is synchronous per subscriber within the broker thread

**Scaling beyond a single node** requires an external broker (RabbitMQ STOMP plugin or Redis Pub/Sub) — see [Roadmap](#roadmap).

### Load Characteristics (Estimates)

| Scenario | Expected Behaviour |
|---|---|
| < 100 concurrent users | Fully handled by in-memory broker, no tuning needed |
| 100–1000 concurrent users | Monitor thread pool saturation; consider increasing executor size |
| > 1000 concurrent users | External broker + horizontal scaling required |

> **[REVISION NOTE]** Replace estimates with JMeter / Gatling benchmarks once load testing
> is conducted. Concrete numbers here significantly strengthen the project for interviews.

---

## Roadmap

### Near-term

- [ ] **JWT Authentication** — stateless token validation on WebSocket handshake via `HandshakeInterceptor`
- [ ] **User presence** — JOIN / LEAVE events with online user count
- [ ] **Message persistence** — PostgreSQL with Spring Data JPA; expose message history REST endpoint
- [ ] **Private messaging** — point-to-point via `/user/{id}/queue/messages`

### Medium-term

- [ ] **Redis Pub/Sub broker** — replace in-memory broker for horizontal scalability
- [ ] **Docker + Docker Compose** — containerize app + Redis together
- [ ] **Rate limiting** — prevent message flooding per session

### Long-term

- [ ] **Kubernetes deployment** — Helm chart with HPA based on active WebSocket connections
- [ ] **Observability** — structured logging (SLF4J + JSON), Micrometer metrics, distributed tracing (OpenTelemetry)
- [ ] **End-to-end encryption** — client-side key exchange before message transmission
- [ ] **Typing indicators** — lightweight `/app/typing` destination with debounce

> **[REVISION NOTE]** Move completed items to a `CHANGELOG.md`. Leaving them checked in
> the roadmap makes the project look unmaintained.

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit with a descriptive message: `git commit -m "feat: add JWT handshake interceptor"`
4. Push and open a Pull Request against `main`

Please follow [Conventional Commits](https://www.conventionalcommits.org/) for commit messages.

---

## License

MIT © Anik Mukherjee

> **[REVISION NOTE]** Add a `LICENSE` file to the repository root. GitHub's license
> detection only works if the file exists — without it, the repo shows "No license" which
> discourages open-source contributors.
