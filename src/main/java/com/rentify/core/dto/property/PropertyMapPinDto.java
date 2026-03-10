package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.PropertyMarketType;
import com.rentify.core.enums.RentalType;

import java.math.BigDecimal;

public record PropertyMapPinDto(
        Long id,
        String title,
        @JsonProperty("propertyType") String propertyType,
        @JsonProperty("marketType") PropertyMarketType marketType,
        @JsonProperty("rentalType") RentalType rentalType,
        BigDecimal lat,
        BigDecimal lng,
        BigDecimal price,
        String currency,
        @JsonProperty("isTopPromoted") Boolean isTopPromoted,
        @JsonProperty("averageRating") BigDecimal averageRating,
        @JsonProperty("reviewCount") Long reviewCount
) {
}
