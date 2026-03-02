package com.rentify.core.dto;

import java.time.LocalDate;

public record BookingRequestDto(
        Long propertyId,
        LocalDate dateFrom,
        LocalDate dateTo,
        Short guests
) {}