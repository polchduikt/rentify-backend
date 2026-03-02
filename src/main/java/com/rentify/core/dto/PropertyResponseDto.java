package com.rentify.core.dto;

import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.RentalType;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

public record PropertyResponseDto(
        Long id,
        Long hostId,
        AddressDto address,
        String title,
        String description,
        RentalType rentalType,
        PropertyStatus status,
        String propertyType,
        Short rooms,
        Short floor,
        Short totalFloors,
        BigDecimal areaSqm,
        Short maxGuests,
        LocalTime checkInTime,
        LocalTime checkOutTime,
        PropertyPricingDto pricing,
        PropertyRuleDto rules,
        List<PropertyPhotoDto> photos,
        Set<AmenityDto> amenities,
        ZonedDateTime createdAt
) {}
