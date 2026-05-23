package com.chat.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for the chat application.
 *
 * Flow: Client connects via SockJS → STOMP handshake at /chat
 *       → sends to /app/...  → @MessageMapping handler processes it
 *       → broker broadcasts to /topic/... → all subscribers receive it
 *
 * @EnableWebSocketMessageBroker turns on the full STOMP message-broker machinery.
 * Without it, @MessageMapping and SimpMessagingTemplate won't work.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Registers the STOMP WebSocket endpoint that clients connect to.
     *
     * - Endpoint : /chat  → ws://localhost:8080/chat (or via SockJS fallback)
     * - SockJS   : wraps WebSocket with HTTP long-polling / iframe fallbacks for
     *              browsers that don't support native WebSocket (IE, some proxies).
     * - Origins  : Only localhost:8080 is allowed; add production domain here later.
     *
     * Revision note: to support multiple origins, replace setAllowedOrigins(...)
     * with setAllowedOriginPatterns("*") during dev, or list each prod URL explicitly.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
                .setAllowedOrigins("http://localhost:8080")
                .withSockJS(); // SockJS fallback for non-WebSocket environments
    }

    /**
     * Configures the message routing/broker layer.
     *
     * Simple broker (in-memory):
     *   - enableSimpleBroker("/topic") activates an in-memory broker.
     *   - Any destination prefixed with /topic is handled by this broker.
     *   - Clients subscribe to  /topic/messages  to receive broadcasts.
     *   - Revision note: replace with enableStompBrokerRelay(...) when
     *     switching to an external broker (RabbitMQ / ActiveMQ) for production.
     *
     * Application destination prefix:
     *   - Messages sent to /app/... are routed to @MessageMapping methods.
     *   - e.g. client sends to /app/sendmessage → handled by
     *     @MessageMapping("/sendmessage") in a @Controller class.
     *   - The /app prefix is stripped before matching the @MessageMapping value.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory broker: handles subscriptions to /topic/**
        registry.enableSimpleBroker("/topic");

        // Route messages prefixed with /app to @MessageMapping handler methods
        registry.setApplicationDestinationPrefixes("/app");
    }
}
