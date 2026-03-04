package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public record AvailabilityBlockDto(
        Long id,
        @JsonProperty("propertyId") Long propertyId,
        @JsonProperty("dateFrom") LocalDate dateFrom,
        @JsonProperty("dateTo") LocalDate dateTo,
        String reason,
        @JsonProperty("createdById") Long createdById,
        @JsonProperty("createdAt") ZonedDateTime createdAt
) {}
