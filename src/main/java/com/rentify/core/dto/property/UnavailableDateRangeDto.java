package com.rentify.core.dto.property;

import com.rentify.core.enums.BookingStatus;

import java.time.LocalDate;

public record UnavailableDateRangeDto(
        LocalDate dateFrom,
        LocalDate dateTo,
        String source,
        BookingStatus bookingStatus
) {
}
