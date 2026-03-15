package com.rentify.core.dto.wallet;

import com.rentify.core.enums.SubscriptionPlan;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Wallet balance payload")
public record WalletBalanceDto(
        @Schema(description = "Balance", example = "100.0")
        BigDecimal balance,
        @Schema(description = "Currency", example = "Sample value")
        String currency,
        @Schema(description = "Subscription plan", example = "FREE")
        SubscriptionPlan subscriptionPlan,
        ZonedDateTime subscriptionActiveUntil
) {
}
