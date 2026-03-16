package com.rentify.core.dto.promotion;

import com.rentify.core.enums.SubscriptionPlan;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Subscription purchase response payload")
public record SubscriptionPurchaseResponseDto(
        @Schema(description = "Subscription plan", example = "FREE")
        SubscriptionPlan subscriptionPlan,
        @Schema(description = "Subscription active until", example = "2026-03-15T10:30:00+02:00")
        ZonedDateTime subscriptionActiveUntil,
        @Schema(description = "Charged amount", example = "100.0")
        BigDecimal chargedAmount,
        @Schema(description = "Balance after", example = "100.0")
        BigDecimal balanceAfter,
        String currency
) {
}
