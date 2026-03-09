package com.rentify.core.dto.wallet;

import com.rentify.core.enums.SubscriptionPlan;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record WalletBalanceDto(
        BigDecimal balance,
        String currency,
        SubscriptionPlan subscriptionPlan,
        ZonedDateTime subscriptionActiveUntil
) {
}
