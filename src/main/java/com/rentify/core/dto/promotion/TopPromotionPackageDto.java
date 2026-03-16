package com.rentify.core.dto.promotion;

import com.rentify.core.enums.TopPromotionPackageType;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Top promotion package payload")
public record TopPromotionPackageDto(
        @Schema(description = "Package type", example = "BASIC")
        TopPromotionPackageType packageType,
        @Schema(description = "Duration days", example = "1")
        Integer durationDays,
        @Schema(description = "Price", example = "12000.00")
        BigDecimal price,
        String currency
) {
}
