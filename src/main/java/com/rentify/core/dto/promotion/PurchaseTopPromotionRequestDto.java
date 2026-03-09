package com.rentify.core.dto.promotion;

import com.rentify.core.enums.TopPromotionPackageType;
import jakarta.validation.constraints.NotNull;

public record PurchaseTopPromotionRequestDto(
        @NotNull(message = "Top promotion package is required")
        TopPromotionPackageType packageType
) {
}
