package com.rentify.core.dto;

import com.rentify.core.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record PaymentDto(
        Long id,
        Long bookingId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String provider,
        ZonedDateTime createdAt
) {}