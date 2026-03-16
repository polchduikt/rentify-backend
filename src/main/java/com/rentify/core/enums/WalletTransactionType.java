package com.rentify.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Wallet transaction operation type.",
        example = "TOP_UP",
        allowableValues = {"TOP_UP", "TOP_PROMOTION", "SUBSCRIPTION"}
)
public enum WalletTransactionType
{
    TOP_UP,
    TOP_PROMOTION,
    SUBSCRIPTION
}
