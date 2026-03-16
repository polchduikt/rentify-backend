package com.rentify.core.dto.property;

import com.rentify.core.enums.BookingStatus;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Unavailable date range payload")
public record UnavailableDateRangeDto(
        @Schema(description = "Date from", example = "2026-03-20")
        LocalDate dateFrom,
        @Schema(description = "Date to", example = "2026-03-20")
        LocalDate dateTo,
        @Schema(description = "Source", example = "Sample value")
        String source,
        BookingStatus bookingStatus
) {
}
