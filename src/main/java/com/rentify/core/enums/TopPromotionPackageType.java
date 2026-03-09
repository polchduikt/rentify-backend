package com.rentify.core.enums;

import java.math.BigDecimal;

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
