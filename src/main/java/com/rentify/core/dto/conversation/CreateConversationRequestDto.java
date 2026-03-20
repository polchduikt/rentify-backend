package com.rentify.core.dto.conversation;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Create conversation request payload")
public record CreateConversationRequestDto(
        @NotNull(message = "Property id is required")
        @Positive(message = "Property id must be positive")
        @JsonProperty("propertyId")
        @Schema(description = "Property id", example = "42")
        Long propertyId
) {}
