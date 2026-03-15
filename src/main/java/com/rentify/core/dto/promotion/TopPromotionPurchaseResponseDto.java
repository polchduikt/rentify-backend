package com.rentify.core.dto.promotion;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Top promotion purchase response payload")
public record TopPromotionPurchaseResponseDto(
        @Schema(description = "Property id", example = "42")
        Long propertyId,
        @Schema(description = "Is top promoted", example = "true")
        Boolean isTopPromoted,
        @Schema(description = "Top promoted until", example = "2026-03-15T10:30:00+02:00")
        ZonedDateTime topPromotedUntil,
        @Schema(description = "Charged amount", example = "100.0")
        BigDecimal chargedAmount,
        @Schema(description = "Balance after", example = "100.0")
        BigDecimal balanceAfter,
        String currency
) {
}
