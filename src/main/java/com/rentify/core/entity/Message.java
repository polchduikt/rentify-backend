package com.rentify.core.entity;

import com.rentify.core.enums.MessageType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "messages",
        indexes = {
                @Index(name = "idx_messages_conversation_id", columnList = "conversation_id"),
                @Index(name = "idx_messages_sender_id", columnList = "sender_id"),
                @Index(name = "idx_messages_conversation_created_at", columnList = "conversation_id, created_at")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Message extends CreatedAtEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @NotNull
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @NotNull
    private User sender;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private MessageType type = MessageType.TEXT;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    @NotNull
    private Boolean isRead = false;

    @Size(max = 800)
    @Column(name = "media_url", length = 800)
    private String mediaUrl;
}
