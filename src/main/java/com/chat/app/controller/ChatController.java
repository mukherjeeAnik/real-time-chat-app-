package com.chat.app.controller;

import com.chat.app.model.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ChatController handles two distinct concerns:
 *
 *  1. WebSocket/STOMP messaging  — receiving chat messages and broadcasting them.
 *  2. HTTP view routing          — serving the chat HTML page.
 *
 * Revision note: if the app grows, consider splitting these into a dedicated
 * WebSocket controller and a separate MVC controller for cleaner separation.
 */
@Controller
public class ChatController {

    /**
     * Handles incoming STOMP messages sent by clients to /app/sendMessage.
     *
     * How the routing works:
     *   Client publishes to  →  /app/sendMessage
     *   /app prefix is stripped by the broker (configured in WebSocketConfig)
     *   Remaining /sendMessage matches this @MessageMapping
     *   Return value is forwarded to the destination in @SendTo
     *
     * @SendTo("/topic/messages"):
     *   Broadcasts the returned ChatMessage to ALL subscribers of /topic/messages.
     *   This is a fan-out (pub-sub) model — every connected client receives it.
     *
     * Revision note: replace @SendTo with SimpMessagingTemplate.convertAndSendToUser()
     * if you later need private/direct messaging instead of broadcast.
     *
     * Revision note: add validation (@Valid / manual checks) before returning,
     * so empty or malformed messages don't get broadcast.
     *
     * @param message  Automatically deserialized from the STOMP message payload (JSON → ChatMessage).
     * @return         The same message object, broadcast to all /topic/messages subscribers.
     */
    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        // Currently echoes the message as-is.
        // Revision note: add a timestamp, sender validation, or persistence (save to DB) here.
        return message;
    }

    /**
     * Serves the chat UI page via a standard HTTP GET request.
     *
     * GET /chat  →  resolves to src/main/resources/templates/chat.html
     *               (Thymeleaf / template engine resolves the "chat" view name)
     *
     * Revision note: the @GetMapping value should start with "/" for clarity
     * i.e. @GetMapping("/chat") — both work, but explicit slashes are conventional.
     *
     * @return  Logical view name; resolved to the actual template by Spring MVC.
     */
    @GetMapping("chat")   // TODO: consider making this "/chat" for consistency
    public String chat() {
        return "chat"; // resolves to templates/chat.html
    }
}
