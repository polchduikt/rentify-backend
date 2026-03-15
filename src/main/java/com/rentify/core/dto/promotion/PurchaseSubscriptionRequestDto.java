package com.rentify.core.dto.promotion;

import com.rentify.core.enums.SubscriptionPackageType;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Purchase subscription request payload")
public record PurchaseSubscriptionRequestDto(
        @NotNull(message = "Subscription package is required")
        SubscriptionPackageType packageType
) {
}
