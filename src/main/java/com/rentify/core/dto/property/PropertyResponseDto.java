package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.PropertyMarketType;
import com.rentify.core.enums.RentalType;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

public record PropertyResponseDto(
        Long id,
        @JsonProperty("hostId") Long hostId,
        AddressDto address,
        String title,
        String description,
        @JsonProperty("rentalType") RentalType rentalType,
        PropertyStatus status,
        @JsonProperty("propertyType") String propertyType,
        @JsonProperty("marketType") PropertyMarketType marketType,
        @JsonProperty("isVerifiedProperty") Boolean isVerifiedProperty,
        @JsonProperty("isVerifiedRealtor") Boolean isVerifiedRealtor,
        @JsonProperty("isDuplicate") Boolean isDuplicate,
        @JsonProperty("isTopPromoted") Boolean isTopPromoted,
        @JsonProperty("viewCount") Long viewCount,
        @JsonProperty("reviewCount") Long reviewCount,
        @JsonProperty("averageRating") BigDecimal averageRating,
        @JsonProperty("topPromotedUntil") ZonedDateTime topPromotedUntil,
        Short rooms,
        Short floor,
        @JsonProperty("totalFloors") Short totalFloors,
        @JsonProperty("areaSqm") BigDecimal areaSqm,
        @JsonProperty("maxGuests") Short maxGuests,
        @JsonProperty("checkInTime") LocalTime checkInTime,
        @JsonProperty("checkOutTime") LocalTime checkOutTime,
        PropertyPricingDto pricing,
        PropertyRuleDto rules,
        List<PropertyPhotoDto> photos,
        Set<AmenityDto> amenities,
        @JsonProperty("createdAt") ZonedDateTime createdAt,
        @JsonProperty("updatedAt") ZonedDateTime updatedAt
) {}
