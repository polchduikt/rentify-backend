package com.rentify.core.service.impl;

import com.rentify.core.dto.booking.BookingDto;
import com.rentify.core.dto.booking.BookingRequestDto;
import com.rentify.core.entity.AvailabilityBlock;
import com.rentify.core.entity.Booking;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.enums.BookingStatus;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.RentalType;
import com.rentify.core.mapper.BookingMapper;
import com.rentify.core.repository.AvailabilityBlockRepository;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.BookingService;
import com.rentify.core.validation.BookingValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final AuthenticationService authService;
    private final BookingMapper bookingMapper;
    private final AvailabilityBlockRepository availabilityRepository;
    private final BookingValidator bookingValidator;

    @Override
    @Transactional
    public BookingDto createBooking(BookingRequestDto request) {
        bookingValidator.validateCreateBookingRequest(request);
        User tenant = authService.getCurrentUser();
        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        if (property.getStatus() != PropertyStatus.ACTIVE) {
            throw new IllegalStateException("Only active properties can be booked.");
        }
        if (property.getRentalType() != RentalType.SHORT_TERM) {
            throw new IllegalStateException("Only short-term properties can be booked.");
        }
        if (property.getHost().getId().equals(tenant.getId())) {
            throw new IllegalArgumentException("You cannot book your own property.");
        }

        long nights = ChronoUnit.DAYS.between(request.dateFrom(), request.dateTo());
        if (property.getMaxGuests() == null) {
            throw new IllegalStateException("Property configuration is invalid: maxGuests is not set.");
        }
        if (request.guests() > property.getMaxGuests()) {
            throw new IllegalArgumentException("Guest count exceeds the maximum capacity of " + property.getMaxGuests() +
                    " for this property.");
        }
        List<AvailabilityBlock> overlappingBlocks = availabilityRepository
                .findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(
                        property.getId(), request.dateTo(), request.dateFrom());
        if (!overlappingBlocks.isEmpty()) {
            throw new IllegalStateException("The property is blocked by the host for the selected dates.");
        }
        List<BookingStatus> ignoredStatuses = List.of(BookingStatus.CANCELLED, BookingStatus.REJECTED);
        boolean isOccupied = bookingRepository.hasOverlappingBookings(
                property.getId(),
                request.dateFrom(),
                request.dateTo(),
                ignoredStatuses
        );
        if (isOccupied) {
            throw new IllegalStateException("The property is already booked for the selected dates.");
        }
        if (property.getPricing() == null || property.getPricing().getPricePerNight() == null) {
            throw new IllegalStateException("Property configuration is invalid: pricePerNight is not set.");
        }
        BigDecimal pricePerNight = property.getPricing().getPricePerNight();
        BigDecimal totalPrice = pricePerNight.multiply(BigDecimal.valueOf(nights));
        Booking booking = Booking.builder()
                .property(property)
                .tenant(tenant)
                .dateFrom(request.dateFrom())
                .dateTo(request.dateTo())
                .guests(request.guests())
                .totalPrice(totalPrice)
                .status(BookingStatus.CREATED)
                .build();
        try {
            booking = bookingRepository.saveAndFlush(booking);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Race condition: The property was just booked by someone else for these dates. " +
                    "Please choose different dates.");
        }
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long id) {
        return bookingRepository.findById(id)
                .map(bookingMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> getMyBookings(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        return bookingRepository.findAllByTenantId(currentUser.getId(), pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    @Transactional
    public BookingDto cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        User currentUser = authService.getCurrentUser();
        if (!booking.getTenant().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only cancel your own bookings");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> getIncomingBookings(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        return bookingRepository.findAllByPropertyHostId(currentUser.getId(), pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    @Transactional
    public BookingDto confirmBooking(Long id) {
        Booking booking = processHostAction(id, BookingStatus.CONFIRMED);
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional
    public BookingDto rejectBooking(Long id) {
        Booking booking = processHostAction(id, BookingStatus.REJECTED);
        return bookingMapper.toDto(booking);
    }

    private Booking processHostAction(Long bookingId, BookingStatus newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        User currentUser = authService.getCurrentUser();
        if (!booking.getProperty().getHost().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You are not the host of this property");
        }
        if (booking.getStatus() != BookingStatus.CREATED) {
            throw new IllegalStateException("You can only change the status of CREATED bookings");
        }
        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }
}
