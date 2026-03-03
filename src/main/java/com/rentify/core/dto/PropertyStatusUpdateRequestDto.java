package com.rentify.core.dto;

import com.rentify.core.enums.PropertyStatus;
import jakarta.validation.constraints.NotNull;

public record PropertyStatusUpdateRequestDto(
        @NotNull(message = "Status is required")
        PropertyStatus status
) {}