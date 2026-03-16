package com.rentify.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Booking lifecycle status.",
        example = "CONFIRMED",
        allowableValues = {"CREATED", "CONFIRMED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "REJECTED"}
)
public enum BookingStatus
{
    CREATED,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    REJECTED
}
