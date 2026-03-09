package com.rentify.core.dto.promotion;

import com.rentify.core.enums.TopPromotionPackageType;

import java.math.BigDecimal;

public record TopPromotionPackageDto(
        TopPromotionPackageType packageType,
        Integer durationDays,
        BigDecimal price,
        String currency
) {
}
