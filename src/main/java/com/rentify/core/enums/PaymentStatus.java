package com.rentify.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Payment transaction status.",
        example = "PAID",
        allowableValues = {"PENDING", "PAID", "FAILED", "REFUNDED", "CANCELLED"}
)
public enum PaymentStatus
{
    PENDING,
    PAID,
    FAILED,
    REFUNDED,
    CANCELLED
}
