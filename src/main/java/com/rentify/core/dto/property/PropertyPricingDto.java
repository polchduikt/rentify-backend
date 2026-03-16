package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Property pricing payload")
public record PropertyPricingDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "Price per night", example = "12000.00")
        @JsonProperty("pricePerNight") @PositiveOrZero BigDecimal pricePerNight,
        @Schema(description = "Price per month", example = "12000.00")
        @JsonProperty("pricePerMonth") @PositiveOrZero BigDecimal pricePerMonth,
        @NotBlank
        @Schema(description = "Currency", example = "Sample value")
        String currency,
        @Schema(description = "Security deposit", example = "100.0")
        @JsonProperty("securityDeposit") @PositiveOrZero BigDecimal securityDeposit,
        @Schema(description = "Cleaning fee", example = "100.0")
        @JsonProperty("cleaningFee") @PositiveOrZero BigDecimal cleaningFee,
        @Schema(description = "Updated at", example = "2026-03-15T10:30:00+02:00")
        @JsonProperty("updatedAt") ZonedDateTime updatedAt
) {}
