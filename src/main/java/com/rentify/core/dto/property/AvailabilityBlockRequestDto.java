package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AvailabilityBlockRequestDto(
        @NotNull(message = "Start date is required")
        @JsonProperty("dateFrom")
        LocalDate dateFrom,

        @NotNull(message = "End date is required")
        @JsonProperty("dateTo")
        LocalDate dateTo,

        @JsonProperty("reason")
        String reason
) {}
