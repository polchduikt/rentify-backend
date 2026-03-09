package com.rentify.core.dto.promotion;

import com.rentify.core.enums.SubscriptionPackageType;
import com.rentify.core.enums.SubscriptionPlan;

import java.math.BigDecimal;

public record SubscriptionPackageDto(
        SubscriptionPackageType packageType,
        SubscriptionPlan plan,
        Integer durationDays,
        BigDecimal price,
        String currency
) {
}
