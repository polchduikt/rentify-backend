package com.rentify.core.dto.promotion;

import com.rentify.core.enums.TopPromotionPackageType;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Purchase top promotion request payload")
public record PurchaseTopPromotionRequestDto(
        @NotNull(message = "Top promotion package is required")
        TopPromotionPackageType packageType
) {
}
