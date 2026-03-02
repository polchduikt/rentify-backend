package com.rentify.core.dto;

import com.rentify.core.enums.MessageType;
import java.time.ZonedDateTime;

public record MessageDto(
        Long id,
        Long conversationId,
        Long senderId,
        MessageType type,
        String text,
        String mediaUrl,
        ZonedDateTime createdAt
) {}