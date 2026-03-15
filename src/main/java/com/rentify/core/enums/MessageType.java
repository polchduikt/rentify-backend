package com.rentify.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Message content type in conversation.",
        example = "TEXT",
        allowableValues = {"TEXT", "IMAGE", "SYSTEM"}
)
public enum MessageType
{
    TEXT,
    IMAGE,
    SYSTEM
}
