package com.rentify.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(
        description = "Top-promotion package option with duration and price.",
        example = "TOP_7_DAYS",
        allowableValues = {"TOP_7_DAYS", "TOP_14_DAYS", "TOP_30_DAYS"}
)
public enum TopPromotionPackageType
{
    TOP_7_DAYS(7, new BigDecimal("99.00")),
    TOP_14_DAYS(14, new BigDecimal("179.00")),
    TOP_30_DAYS(30, new BigDecimal("299.00"));

    private final int durationDays;
    private final BigDecimal price;

    TopPromotionPackageType(int durationDays, BigDecimal price) {
        this.durationDays = durationDays;
        this.price = price;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
