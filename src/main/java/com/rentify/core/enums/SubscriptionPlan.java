package com.rentify.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Current user subscription level.",
        example = "FREE",
        allowableValues = {"FREE", "BASIC", "PREMIUM"}
)
public enum SubscriptionPlan
{
    FREE,
    BASIC,
    PREMIUM
}
