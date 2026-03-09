package com.rentify.core.dto.promotion;

import com.rentify.core.enums.SubscriptionPackageType;
import jakarta.validation.constraints.NotNull;

public record PurchaseSubscriptionRequestDto(
        @NotNull(message = "Subscription package is required")
        SubscriptionPackageType packageType
) {
}
