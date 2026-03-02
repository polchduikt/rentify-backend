package com.rentify.core.dto;

import com.rentify.core.enums.RentalType;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record PropertyCreateRequestDto(
        AddressDto address,
        String title,
        String description,
        RentalType rentalType,
        String propertyType,
        Short rooms,
        Short floor,
        Short totalFloors,
        BigDecimal areaSqm,
        Short maxGuests,
        LocalTime checkInTime,
        List<Long> amenityIds,
        LocalTime checkOutTime,
        PropertyPricingDto pricing,
        PropertyRuleDto rules
) {}