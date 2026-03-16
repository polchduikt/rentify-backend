package com.rentify.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Wallet transaction direction relative to user balance.",
        example = "CREDIT",
        allowableValues = {"CREDIT", "DEBIT"}
)
public enum WalletTransactionDirection
{
    CREDIT,
    DEBIT
}
