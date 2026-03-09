package com.rentify.core.enums;

import java.math.BigDecimal;

public enum SubscriptionPackageType
{
    BASIC_30_DAYS(SubscriptionPlan.BASIC, 30, new BigDecimal("199.00")),
    BASIC_90_DAYS(SubscriptionPlan.BASIC, 90, new BigDecimal("499.00")),
    PREMIUM_30_DAYS(SubscriptionPlan.PREMIUM, 30, new BigDecimal("399.00")),
    PREMIUM_90_DAYS(SubscriptionPlan.PREMIUM, 90, new BigDecimal("999.00"));

    private final SubscriptionPlan plan;
    private final int durationDays;
    private final BigDecimal price;

    SubscriptionPackageType(SubscriptionPlan plan, int durationDays, BigDecimal price) {
        this.plan = plan;
        this.durationDays = durationDays;
        this.price = price;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
