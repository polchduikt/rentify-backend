package com.rentify.core.dto;

import com.rentify.core.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public record BookingDto(
        Long id,
        Long propertyId,
        Long tenantId,
        LocalDate dateFrom,
        LocalDate dateTo,
        Short guests,
        BigDecimal totalPrice,
        BookingStatus status,
        ZonedDateTime createdAt
) {}
