package com.rentify.core.dto;

import com.rentify.core.enums.RequestStatus;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record LongTermRequestDto(
        Long id,
        Long propertyId,
        Long tenantId,
        LocalDate preferredDate,
        String message,
        RequestStatus status,
        ZonedDateTime createdAt
) {}
