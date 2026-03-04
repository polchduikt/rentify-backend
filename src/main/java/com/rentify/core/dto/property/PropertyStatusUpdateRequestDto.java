package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.PropertyStatus;
import jakarta.validation.constraints.NotNull;

public record PropertyStatusUpdateRequestDto(
        @NotNull(message = "Status is required")
        @JsonProperty("status")
        PropertyStatus status
) {}
