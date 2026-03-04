package com.rentify.core.dto.conversation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.MessageType;
import java.time.ZonedDateTime;

public record MessageDto(
        Long id,
        @JsonProperty("conversationId") Long conversationId,
        @JsonProperty("senderId") Long senderId,
        MessageType type,
        String text,
        @JsonProperty("isRead") Boolean isRead,
        @JsonProperty("mediaUrl") String mediaUrl,
        @JsonProperty("createdAt") ZonedDateTime createdAt
) {}
