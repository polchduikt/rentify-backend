package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record PropertyPricingDto(
        Long id,
        @JsonProperty("pricePerNight") @PositiveOrZero BigDecimal pricePerNight,
        @JsonProperty("pricePerMonth") @PositiveOrZero BigDecimal pricePerMonth,
        @NotBlank
        String currency,
        @JsonProperty("securityDeposit") @PositiveOrZero BigDecimal securityDeposit,
        @JsonProperty("cleaningFee") @PositiveOrZero BigDecimal cleaningFee,
        @JsonProperty("updatedAt") ZonedDateTime updatedAt
) {}
