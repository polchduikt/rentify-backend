package com.rentify.core.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AvailabilityBlockRequestDto(
        @NotNull(message = "Start date is required")
        LocalDate dateFrom,
        @NotNull(message = "End date is required")
        LocalDate dateTo,
        String reason
) {}