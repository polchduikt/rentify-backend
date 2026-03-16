package com.rentify.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Amenity category used for grouping and filtering amenities.",
        example = "BASIC",
        allowableValues = {
                "BASIC", "VERIFICATION", "RENOVATION", "ACCESSIBILITY", "BLACKOUT_SUPPORT",
                "LIVING_CONDITIONS", "LAYOUT", "WALL_TYPE", "HEATING", "OFFER_TYPE",
                "RENTAL_TERMS", "OTHER"
        }
)
public enum AmenityCategory {
    BASIC,
    VERIFICATION,
    RENOVATION,
    ACCESSIBILITY,
    BLACKOUT_SUPPORT,
    LIVING_CONDITIONS,
    LAYOUT,
    WALL_TYPE,
    HEATING,
    OFFER_TYPE,
    RENTAL_TERMS,
    OTHER
}
