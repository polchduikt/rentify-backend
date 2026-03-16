package com.rentify.core.dto.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Booking request payload")
public record BookingRequestDto(
        @NotNull(message = "Property id is required")
        @JsonProperty("propertyId")
        @Schema(description = "Property id", example = "42")
        Long propertyId,

        @NotNull(message = "Check-in date is required")
        @FutureOrPresent(message = "Check-in date cannot be in the past")
        @JsonProperty("dateFrom")
        @Schema(description = "Date from", example = "2026-03-20")
        LocalDate dateFrom,

        @NotNull(message = "Check-out date is required")
        @FutureOrPresent(message = "Check-out date cannot be in the past")
        @JsonProperty("dateTo")
        @Schema(description = "Date to", example = "2026-03-20")
        LocalDate dateTo,

        @NotNull(message = "Guests count is required")
        @Min(value = 1, message = "Guests must be at least 1")
        @JsonProperty("guests")
        @Schema(description = "Guests", example = "1")
        Short guests
) {}
