package com.rentify.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Property publication status in listing workflow.",
        example = "ACTIVE",
        allowableValues = {"DRAFT", "ACTIVE", "INACTIVE", "BLOCKED"}
)
public enum PropertyStatus
{
    DRAFT,
    ACTIVE,
    INACTIVE,
    BLOCKED
}
