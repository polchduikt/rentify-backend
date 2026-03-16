package com.rentify.core.dto.conversation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.MessageType;
import java.time.ZonedDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Message payload")
public record MessageDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "Conversation id", example = "42")
        @JsonProperty("conversationId") Long conversationId,
        @Schema(description = "Sender id", example = "42")
        @JsonProperty("senderId") Long senderId,
        @Schema(description = "Type", example = "TEXT")
        MessageType type,
        @Schema(description = "Text", example = "Sample value")
        String text,
        @Schema(description = "Is read", example = "true")
        @JsonProperty("isRead") Boolean isRead,
        @Schema(description = "Media url", example = "https://example.com/resource.jpg")
        @JsonProperty("mediaUrl") String mediaUrl,
        @Schema(description = "Created at", example = "2026-03-15T10:30:00+02:00")
        @JsonProperty("createdAt") ZonedDateTime createdAt
) {}
