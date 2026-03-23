package com.rentify.core.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Payment creation request payload")
public record PaymentCreateRequestDto(
        @NotNull(message = "Booking ID is required")
        @Positive(message = "Booking ID must be positive")
        @JsonProperty("bookingId")
        @Schema(description = "Booking identifier", example = "55")
        Long bookingId
) {}
