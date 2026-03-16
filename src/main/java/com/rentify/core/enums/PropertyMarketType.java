package com.rentify.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Property market segment.",
        example = "SECONDARY",
        allowableValues = {"SECONDARY", "NEW_BUILD"}
)
public enum PropertyMarketType {
    SECONDARY,
    NEW_BUILD
}
