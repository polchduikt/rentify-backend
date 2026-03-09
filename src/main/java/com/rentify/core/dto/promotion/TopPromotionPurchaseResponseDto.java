package com.rentify.core.dto.promotion;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record TopPromotionPurchaseResponseDto(
        Long propertyId,
        Boolean isTopPromoted,
        ZonedDateTime topPromotedUntil,
        BigDecimal chargedAmount,
        BigDecimal balanceAfter,
        String currency
) {
}
