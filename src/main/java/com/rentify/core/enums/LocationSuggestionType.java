package com.rentify.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Type of location suggestion entry.",
        example = "CITY",
        allowableValues = {"CITY", "DISTRICT", "METRO", "RESIDENTIAL_COMPLEX"}
)
public enum LocationSuggestionType {
    CITY,
    DISTRICT,
    METRO,
    RESIDENTIAL_COMPLEX
}
