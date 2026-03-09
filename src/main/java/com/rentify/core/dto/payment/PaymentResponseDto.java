package com.rentify.core.dto.payment;

import com.rentify.core.enums.BookingStatus;
import com.rentify.core.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record PaymentResponseDto(
        Long id,
        Long bookingId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String provider,
        String providerPaymentId,
        BookingStatus bookingStatus,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
}
