package com.rentify.core.dto.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public record BookingDto(
        Long id,
        @JsonProperty("propertyId") Long propertyId,
        @JsonProperty("tenantId") Long tenantId,
        @JsonProperty("dateFrom") LocalDate dateFrom,
        @JsonProperty("dateTo") LocalDate dateTo,
        Short guests,
        @JsonProperty("totalPrice") BigDecimal totalPrice,
        BookingStatus status,
        @JsonProperty("createdAt") ZonedDateTime createdAt,
        @JsonProperty("updatedAt") ZonedDateTime updatedAt
) {}
