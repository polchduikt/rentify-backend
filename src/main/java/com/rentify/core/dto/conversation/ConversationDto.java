package com.rentify.core.dto.conversation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Conversation payload")
public record ConversationDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "Property id", example = "42")
        @JsonProperty("propertyId") Long propertyId,
        @Schema(description = "Host id", example = "42")
        @JsonProperty("hostId") Long hostId,
        @Schema(description = "Tenant id", example = "42")
        @JsonProperty("tenantId") Long tenantId,
        @Schema(description = "Created at", example = "2026-03-15T10:30:00+02:00")
        @JsonProperty("createdAt") ZonedDateTime createdAt
) {}
