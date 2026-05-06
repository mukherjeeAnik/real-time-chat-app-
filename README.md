# 🚀 Real-Time Chat Application

![Build](https://img.shields.io/badge/build-passing-brightgreen)
![Deploy](https://img.shields.io/badge/deploy-render-blue)
![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/SpringBoot-WebSockets-green)
![Protocol](https://img.shields.io/badge/Protocol-STOMP%20%2B%20SockJS-purple)

A **production-style real-time chat system** built using **Spring Boot WebSockets (STOMP over SockJS)** enabling **low-latency, event-driven communication** between concurrent clients. 

---

## ⚡ Why This Project

* Built a **real-time bidirectional messaging system** using **WebSockets (STOMP pub/sub)**
* Implemented **event-driven architecture** with **decoupled message routing (`/app → /topic`)**
* Demonstrates **concurrency handling + low-latency communication** without HTTP polling
* Clean **full-stack integration (Spring Boot + Thymeleaf + JS)** deployed on cloud

---

## 🧠 Key Features

* ⚡ Real-time messaging via WebSockets
* 🔄 Publish-Subscribe model (`/topic/messages`)
* 👥 Multi-user concurrent chat
* 📡 SockJS fallback for cross-browser support
* 🎯 No polling → reduced latency
* 🎨 Minimal responsive UI with Bootstrap

---

## 🏗️ Architecture Diagram 

```
        ┌──────────────────────┐
        │      Browser A       │
        │ (SockJS + STOMP.js) │
        └─────────┬────────────┘
                  │ WebSocket
                  ▼
        ┌──────────────────────┐
        │   Spring Boot App    │
        │  WebSocket Endpoint  │ (/chat)
        └─────────┬────────────┘
                  │
        ┌─────────▼────────────┐
        │  STOMP Message Broker│
        │   (/topic/messages)  │
        └─────────┬────────────┘
                  │ Broadcast
        ┌─────────▼────────────┐
        │      Browser B       │
        │ (Subscribed Client)  │
        └──────────────────────┘
```

---

## 🔄 Message Flow

1. Client connects to `/chat` via WebSocket handshake
2. Sends message → `/app/sendMessage`
3. Server processes message
4. Broker broadcasts → `/topic/messages`
5. All subscribed clients receive update instantly

---

## 🛠️ Tech Stack

**Backend**

* Java 17
* Spring Boot
* Spring WebSocket
* STOMP Protocol

**Frontend**

* Thymeleaf
* HTML, CSS, JavaScript
* Bootstrap

**Infra / Tools**

* SockJS
* STOMP.js
* Render (Deployment)

---

## 📂 Project Structure

```
chat-app/
├── src/main/java/com/chat/app
│   ├── config/          # WebSocket configuration
│   ├── controller/      # Message endpoints
│   ├── model/           # Chat message model
│   └── AppApplication
│
├── src/main/resources
│   ├── templates/       # Thymeleaf views
│   ├── static/
│   └── application.properties
│
└── pom.xml
```

---

## ⚙️ Run Locally

```bash
git clone https://github.com/your-username/chat-app.git
cd chat-app
mvn spring-boot:run
```

App URL:

```
http://localhost:8080/chat
```

---

## 📡 WebSocket API

| Endpoint           | Purpose               |
| ------------------ | --------------------- |
| `/chat`            | WebSocket handshake   |
| `/app/sendMessage` | Publish message       |
| `/topic/messages`  | Subscribe for updates |

---

## 📸 Demo Screenshot





---
<img width="1920" height="1080" alt="Screenshot (383)" src="https://github.com/user-attachments/assets/9c9a4010-7948-41d2-a469-24217055a77b" />

## 🚀 Future Enhancements

* 🔐 JWT Authentication / User sessions
* 💾 Message persistence (PostgreSQL / MongoDB)
* ⚡ Redis Pub/Sub for horizontal scaling
* 🐳 Docker containerization
* ☁️ Kubernetes deployment
* 📊 Observability (logs, metrics, tracing)

---

## 🧑‍💻 Author

**Nick (Anik Mukherjee)**
Backend Engineer | Distributed Systems Focus

---

## ⭐ Support

If this project helped you, consider giving it a ⭐ on GitHub!
