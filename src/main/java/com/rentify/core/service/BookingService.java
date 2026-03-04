package com.rentify.core.service;

import com.rentify.core.dto.booking.BookingDto;
import com.rentify.core.dto.booking.BookingRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingDto createBooking(BookingRequestDto request);
    BookingDto getBookingById(Long id);
    Page<BookingDto> getMyBookings(Pageable pageable);
    BookingDto cancelBooking(Long id);
    Page<BookingDto> getIncomingBookings(Pageable pageable);
    BookingDto confirmBooking(Long id);
    BookingDto rejectBooking(Long id);
}