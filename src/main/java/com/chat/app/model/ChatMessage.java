package com.chat.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single chat message passed between the client and server.
 *
 * Currently a plain POJO — no persistence layer attached.
 * Jackson (bundled with Spring) automatically serializes/deserializes
 * this to/from JSON over the STOMP WebSocket connection.
 *
 * Revision note: add @Entity + @Table("chat_messages") here when you're
 * ready to persist messages to a database (JPA/Hibernate).
 *
 * Lombok annotations used:
 *   @Data         → generates getters, setters, equals(), hashCode(), toString()
 *   @NoArgsConstructor → generates a no-arg constructor (required by Jackson for deserialization)
 */
@Data
@NoArgsConstructor
public class ChatMessage {

    /**
     * Unique identifier for the message.
     *
     * Revision note: currently unused — no DB or ID-generation logic exists yet.
     * Add @Id + @GeneratedValue(strategy = GenerationType.IDENTITY) when
     * connecting to a database. Until then, this field will always be null.
     */
    private Long id;

    /**
     * The display name or username of the person sending the message.
     *
     * Revision note: this is a free-form string with no validation or auth tie-in.
     * Consider linking it to a User entity / JWT principal later so the
     * sender cannot be spoofed by the client.
     */
    private String sender;

    /**
     * The text body of the chat message.
     *
     * Revision note: no length limit or null check exists yet.
     * Add @NotBlank and @Size(max = 500) (Jakarta validation) to prevent
     * empty or oversized messages from being broadcast.
     */
    private String content;

    /*
     * Revision note — fields to consider adding later:
     *
     *   private LocalDateTime timestamp;  // server-side time the message was received
     *   private MessageType type;         // e.g. CHAT, JOIN, LEAVE — for system events
     *   private String roomId;            // for multi-room / channel support
     */
}
