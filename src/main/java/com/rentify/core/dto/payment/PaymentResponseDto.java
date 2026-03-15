package com.rentify.core.dto.payment;

import com.rentify.core.enums.BookingStatus;
import com.rentify.core.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payment response payload")
public record PaymentResponseDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "Booking id", example = "42")
        Long bookingId,
        @Schema(description = "Amount", example = "100.0")
        BigDecimal amount,
        @Schema(description = "Currency", example = "Sample value")
        String currency,
        @Schema(description = "Status", example = "PENDING")
        PaymentStatus status,
        @Schema(description = "Provider", example = "Sample value")
        String provider,
        @Schema(description = "Provider payment id", example = "Sample value")
        String providerPaymentId,
        @Schema(description = "Booking status", example = "PENDING")
        BookingStatus bookingStatus,
        @Schema(description = "Created at", example = "2026-03-15T10:30:00+02:00")
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
}
