package com.rentify.core.dto.promotion;

import com.rentify.core.enums.SubscriptionPackageType;
import com.rentify.core.enums.SubscriptionPlan;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Subscription package payload")
public record SubscriptionPackageDto(
        @Schema(description = "Package type", example = "MONTHLY")
        SubscriptionPackageType packageType,
        @Schema(description = "Plan", example = "FREE")
        SubscriptionPlan plan,
        @Schema(description = "Duration days", example = "1")
        Integer durationDays,
        @Schema(description = "Price", example = "12000.00")
        BigDecimal price,
        String currency
) {
}
