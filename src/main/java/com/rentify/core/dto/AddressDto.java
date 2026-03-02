package com.rentify.core.dto;

import java.math.BigDecimal;

public record AddressDto(
        Long id,
        LocationDto location,
        String street,
        String houseNumber,
        String apartment,
        String postalCode,
        BigDecimal lat,
        BigDecimal lng
) {}