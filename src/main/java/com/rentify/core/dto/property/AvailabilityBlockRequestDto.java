package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Availability block request payload")
public record AvailabilityBlockRequestDto(
        @NotNull(message = "Start date is required")
        @JsonProperty("dateFrom")
        @Schema(description = "Date from", example = "2026-03-20")
        LocalDate dateFrom,

        @NotNull(message = "End date is required")
        @JsonProperty("dateTo")
        @Schema(description = "Date to", example = "2026-03-20")
        LocalDate dateTo,

        @JsonProperty("reason")
        @Schema(description = "Reason", example = "Sample value")
        String reason
) {}
