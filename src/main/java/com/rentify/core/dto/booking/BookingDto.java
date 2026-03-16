package com.rentify.core.dto.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Booking payload")
public record BookingDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "Property id", example = "42")
        @JsonProperty("propertyId") Long propertyId,
        @Schema(description = "Tenant id", example = "42")
        @JsonProperty("tenantId") Long tenantId,
        @Schema(description = "Date from", example = "2026-03-20")
        @JsonProperty("dateFrom") LocalDate dateFrom,
        @Schema(description = "Date to", example = "2026-03-20")
        @JsonProperty("dateTo") LocalDate dateTo,
        @Schema(description = "Guests", example = "1")
        Short guests,
        @Schema(description = "Total price", example = "12000.00")
        @JsonProperty("totalPrice") BigDecimal totalPrice,
        @Schema(description = "Status", example = "PENDING")
        BookingStatus status,
        @Schema(description = "Created at", example = "2026-03-15T10:30:00+02:00")
        @JsonProperty("createdAt") ZonedDateTime createdAt,
        @Schema(description = "Updated at", example = "2026-03-15T10:30:00+02:00")
        @JsonProperty("updatedAt") ZonedDateTime updatedAt
) {}
