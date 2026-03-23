package com.rentify.core.dto.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Booking status update request payload")
public record BookingStatusUpdateRequestDto(
        @NotNull(message = "Status is required")
        @JsonProperty("status")
        @Schema(description = "Target status", example = "CANCELLED")
        BookingStatus status
) {}
