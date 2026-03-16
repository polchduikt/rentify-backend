package com.rentify.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Rental duration model.",
        example = "SHORT_TERM",
        allowableValues = {"LONG_TERM", "SHORT_TERM"}
)
public enum RentalType
{
    LONG_TERM,
    SHORT_TERM
}
