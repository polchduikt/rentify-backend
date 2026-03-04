package com.rentify.core.dto.booking;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BookingRequestDto(
        @NotNull(message = "Property id is required")
        @JsonProperty("propertyId")
        Long propertyId,

        @NotNull(message = "Check-in date is required")
        @FutureOrPresent(message = "Check-in date cannot be in the past")
        @JsonProperty("dateFrom")
        LocalDate dateFrom,

        @NotNull(message = "Check-out date is required")
        @FutureOrPresent(message = "Check-out date cannot be in the past")
        @JsonProperty("dateTo")
        LocalDate dateTo,

        @NotNull(message = "Guests count is required")
        @Min(value = 1, message = "Guests must be at least 1")
        @JsonProperty("guests")
        Short guests
) {}
