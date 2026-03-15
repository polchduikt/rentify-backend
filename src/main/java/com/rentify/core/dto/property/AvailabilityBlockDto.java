package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Availability block payload")
public record AvailabilityBlockDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "Property id", example = "42")
        @JsonProperty("propertyId") Long propertyId,
        @Schema(description = "Date from", example = "2026-03-20")
        @JsonProperty("dateFrom") LocalDate dateFrom,
        @Schema(description = "Date to", example = "2026-03-20")
        @JsonProperty("dateTo") LocalDate dateTo,
        @Schema(description = "Reason", example = "Sample value")
        String reason,
        @Schema(description = "Created by id", example = "42")
        @JsonProperty("createdById") Long createdById,
        @Schema(description = "Created at", example = "2026-03-15T10:30:00+02:00")
        @JsonProperty("createdAt") ZonedDateTime createdAt
) {}
