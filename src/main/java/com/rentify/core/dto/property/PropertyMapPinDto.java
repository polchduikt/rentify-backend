package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.PropertyMarketType;
import com.rentify.core.enums.RentalType;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Property map pin payload")
public record PropertyMapPinDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "Title", example = "Cozy apartment in Kyiv center")
        String title,
        @JsonProperty("propertyType") String propertyType,
        @JsonProperty("marketType") PropertyMarketType marketType,
        @JsonProperty("rentalType") RentalType rentalType,
        @Schema(description = "Lat", example = "100.0")
        BigDecimal lat,
        @Schema(description = "Lng", example = "100.0")
        BigDecimal lng,
        @Schema(description = "Price", example = "12000.00")
        BigDecimal price,
        @Schema(description = "Currency", example = "Sample value")
        String currency,
        @JsonProperty("isTopPromoted") Boolean isTopPromoted,
        @JsonProperty("averageRating") BigDecimal averageRating,
        @JsonProperty("reviewCount") Long reviewCount
) {
}
