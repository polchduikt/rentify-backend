package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.PropertyStatus;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Property status update request payload")
public record PropertyStatusUpdateRequestDto(
        @NotNull(message = "Status is required")
        @JsonProperty("status")
        @Schema(description = "Status", example = "ACTIVE")
        PropertyStatus status
) {}
