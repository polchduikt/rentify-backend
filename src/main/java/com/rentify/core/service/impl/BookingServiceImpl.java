package com.rentify.core.service.impl;

import com.rentify.core.dto.booking.BookingDto;
import com.rentify.core.dto.booking.BookingRequestDto;
import com.rentify.core.entity.AvailabilityBlock;
import com.rentify.core.entity.Booking;
import com.rentify.core.entity.Payment;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.service.CurrencyResolver;
import com.rentify.core.enums.BookingScope;
import com.rentify.core.enums.BookingStatus;
import com.rentify.core.enums.PaymentStatus;
import com.rentify.core.mapper.BookingMapper;
import com.rentify.core.repository.AvailabilityBlockRepository;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.PaymentRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.BookingService;
import com.rentify.core.security.UserRoleUtils;
import com.rentify.core.validation.BookingValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final String REFUND_PROVIDER = "MOCK_GATEWAY";

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final PaymentRepository paymentRepository;
    private final AuthenticationService authService;
    private final BookingMapper bookingMapper;
    private final AvailabilityBlockRepository availabilityRepository;
    private final BookingValidator bookingValidator;
    private final CurrencyResolver currencyResolver;

    @Override
    @Transactional
    public BookingDto createBooking(BookingRequestDto request) {
        bookingValidator.validateCreateBookingRequest(request);
        User tenant = authService.getCurrentUser();
        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        bookingValidator.validateBookingEligibility(property, tenant, request);

        boolean hasBlockedDates = hasBlockedDates(property.getId(), request);
        boolean isOccupied = hasOverlappingBookings(property.getId(), request);
        bookingValidator.validateAvailability(hasBlockedDates, isOccupied);

        BigDecimal totalPrice = calculateTotalPrice(property, request);
        Booking booking = buildBooking(property, tenant, request, totalPrice);
        Booking savedBooking = saveBookingWithRaceProtection(booking);

        log.info("Booking created: bookingId={}, propertyId={}, tenantId={}, totalPrice={}",
                savedBooking.getId(), property.getId(), tenant.getId(), totalPrice);
        return bookingMapper.toDto(savedBooking);
    }

    private boolean hasBlockedDates(Long propertyId, BookingRequestDto request) {
        List<AvailabilityBlock> overlappingBlocks = availabilityRepository
                .findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(
                        propertyId, request.dateTo(), request.dateFrom());
        if (!overlappingBlocks.isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean hasOverlappingBookings(Long propertyId, BookingRequestDto request) {
        List<BookingStatus> ignoredStatuses = List.of(
                BookingStatus.CANCELLED,
                BookingStatus.REJECTED,
                BookingStatus.COMPLETED
        );
        return bookingRepository.hasOverlappingBookings(
                propertyId,
                request.dateFrom(),
                request.dateTo(),
                ignoredStatuses
        );
    }

    private BigDecimal calculateTotalPrice(Property property, BookingRequestDto request) {
        if (property.getPricing() == null || property.getPricing().getPricePerNight() == null) {
            throw new IllegalStateException("Property configuration is invalid: pricePerNight is not set.");
        }
        long nights = ChronoUnit.DAYS.between(request.dateFrom(), request.dateTo());
        BigDecimal pricePerNight = property.getPricing().getPricePerNight();
        return pricePerNight.multiply(BigDecimal.valueOf(nights));
    }

    private Booking buildBooking(Property property, User tenant, BookingRequestDto request, BigDecimal totalPrice) {
        return Booking.builder()
                .property(property)
                .tenant(tenant)
                .dateFrom(request.dateFrom())
                .dateTo(request.dateTo())
                .guests(request.guests())
                .totalPrice(totalPrice)
                .status(BookingStatus.CREATED)
                .build();
    }

    private Booking saveBookingWithRaceProtection(Booking booking) {
        try {
            return bookingRepository.saveAndFlush(booking);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Race condition: The property was just booked by someone else for these dates. " +
                    "Please choose different dates.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        User currentUser = authService.getCurrentUser();
        boolean isTenant = booking.getTenant().getId().equals(currentUser.getId());
        boolean isHost = booking.getProperty().getHost().getId().equals(currentUser.getId());
        boolean isAdmin = UserRoleUtils.isAdmin(currentUser);
        if (!isTenant && !isHost && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to view this booking");
        }
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> getBookings(BookingScope scope, Pageable pageable) {
        return switch (scope) {
            case GUEST -> getMyBookings(pageable);
            case HOST -> getIncomingBookings(pageable);
        };
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
        boolean isTenant = booking.getTenant().getId().equals(currentUser.getId());
        boolean isHost = booking.getProperty().getHost().getId().equals(currentUser.getId());
        if (!isTenant && !isHost) {
            throw new AccessDeniedException("You can only cancel your own or hosted bookings");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.REJECTED) {
            throw new IllegalStateException("Booking is already closed");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Completed booking cannot be cancelled");
        }

        if (isTenant && booking.getStatus() == BookingStatus.IN_PROGRESS) {
            throw new IllegalStateException("Tenant cannot cancel booking in progress");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);
        refundIfPaid(savedBooking);
        log.info("Booking canceled: bookingId={}, actorUserId={}", savedBooking.getId(), currentUser.getId());
        return bookingMapper.toDto(savedBooking);
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
        log.info("Booking confirmed: bookingId={}", booking.getId());
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional
    public BookingDto rejectBooking(Long id) {
        Booking booking = processHostAction(id, BookingStatus.REJECTED);
        log.info("Booking rejected: bookingId={}", booking.getId());
        return bookingMapper.toDto(booking);
    }

    private Booking processHostAction(Long bookingId, BookingStatus newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        User currentUser = authService.getCurrentUser();
        if (!booking.getProperty().getHost().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: you are not the host of this property");
        }
        if (booking.getStatus() != BookingStatus.CREATED) {
            throw new IllegalStateException("You can only change the status of CREATED bookings");
        }
        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }

    private void refundIfPaid(Booking booking) {
        if (!paymentRepository.existsByBookingIdAndStatus(booking.getId(), PaymentStatus.PAID)) {
            return;
        }
        if (paymentRepository.existsByBookingIdAndStatus(booking.getId(), PaymentStatus.REFUNDED)) {
            return;
        }
        if (booking.getTotalPrice() == null) {
            return;
        }

        String currency = currencyResolver.resolvePropertyCurrency(booking.getProperty());

        Payment refund = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalPrice())
                .currency(currency)
                .status(PaymentStatus.REFUNDED)
                .provider(REFUND_PROVIDER)
                .providerPaymentId("mock_refund_" + UUID.randomUUID())
                .build();
        paymentRepository.save(refund);
        log.info("Refund created for canceled booking: bookingId={}, amount={}, currency={}",
                booking.getId(), booking.getTotalPrice(), currency);
    }
}
