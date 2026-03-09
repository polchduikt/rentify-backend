package com.rentify.core.dto.promotion;

import com.rentify.core.enums.SubscriptionPlan;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record SubscriptionPurchaseResponseDto(
        SubscriptionPlan subscriptionPlan,
        ZonedDateTime subscriptionActiveUntil,
        BigDecimal chargedAmount,
        BigDecimal balanceAfter,
        String currency
) {
}
