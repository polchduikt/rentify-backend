package com.rentify.core.dto;

import java.time.LocalDate;

public record AvailabilityBlockDto(
        Long id,
        Long propertyId,
        LocalDate dateFrom,
        LocalDate dateTo,
        String reason
) {}
