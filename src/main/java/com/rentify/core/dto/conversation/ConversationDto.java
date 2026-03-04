package com.rentify.core.dto.conversation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

public record ConversationDto(
        Long id,
        @JsonProperty("propertyId") Long propertyId,
        @JsonProperty("hostId") Long hostId,
        @JsonProperty("tenantId") Long tenantId,
        @JsonProperty("createdAt") ZonedDateTime createdAt
) {}
