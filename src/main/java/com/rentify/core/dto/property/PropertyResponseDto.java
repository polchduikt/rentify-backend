package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.PropertyStatus;
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
