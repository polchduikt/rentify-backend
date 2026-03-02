package com.rentify.core.dto;

import java.math.BigDecimal;

public record PropertyPricingDto(
        BigDecimal pricePerNight,
        BigDecimal pricePerMonth,
        String currency,
        BigDecimal securityDeposit,
        BigDecimal cleaningFee
) {}