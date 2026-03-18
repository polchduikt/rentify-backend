package com.rentify.core.unit;

import com.rentify.core.dto.booking.BookingDto;
import com.rentify.core.dto.booking.BookingRequestDto;
import com.rentify.core.entity.AvailabilityBlock;
import com.rentify.core.entity.Booking;
import com.rentify.core.entity.Payment;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.PropertyPricing;
import com.rentify.core.entity.Role;
import com.rentify.core.entity.User;
import com.rentify.core.enums.BookingStatus;
import com.rentify.core.enums.PaymentStatus;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.RentalType;
import com.rentify.core.mapper.BookingMapper;
import com.rentify.core.repository.AvailabilityBlockRepository;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.PaymentRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.impl.BookingServiceImpl;
import com.rentify.core.validation.BookingValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private AuthenticationService authService;
    @Mock private BookingMapper bookingMapper;
    @Mock private AvailabilityBlockRepository availabilityRepository;
    @Mock private BookingValidator bookingValidator;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User hostUser;
    private User tenantUser;
    private User outsiderUser;
    private User adminUser;
    private Property property;
    private Booking booking;
    private BookingRequestDto request;
    private BookingDto bookingDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        Role userRole = Role.builder().name("ROLE_USER").build();
        hostUser = User.builder().id(1L).roles(Set.of(userRole)).build();
        tenantUser = User.builder().id(2L).roles(Set.of(userRole)).build();
        outsiderUser = User.builder().id(3L).roles(Set.of(userRole)).build();

        Role adminRole = Role.builder().name("ROLE_ADMIN").build();
        adminUser = User.builder().id(4L).roles(Set.of(adminRole)).build();

        PropertyPricing pricing = PropertyPricing.builder()
                .pricePerNight(BigDecimal.valueOf(1000))
                .currency("UAH")
                .build();

        property = Property.builder()
                .id(10L)
                .host(hostUser)
                .status(PropertyStatus.ACTIVE)
                .rentalType(RentalType.SHORT_TERM)
                .maxGuests((short) 3)
                .pricing(pricing)
                .build();
        pricing.setProperty(property);

        request = new BookingRequestDto(
                10L,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 5),
                (short) 2
        );

        booking = Booking.builder()
                .id(50L)
                .property(property)
                .tenant(tenantUser)
                .dateFrom(request.dateFrom())
                .dateTo(request.dateTo())
                .guests(request.guests())
                .totalPrice(BigDecimal.valueOf(4000))
                .status(BookingStatus.CREATED)
                .build();

        bookingDto = new BookingDto(
                50L,
                10L,
                2L,
                request.dateFrom(),
                request.dateTo(),
                (short) 2,
                BigDecimal.valueOf(4000),
                BookingStatus.CREATED,
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("createBooking()")
    class CreateBookingTests {

        @Test
        void shouldCreateBooking_whenRequestIsValid() {
            when(authService.getCurrentUser()).thenReturn(tenantUser);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(availabilityRepository.findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(
                    10L, request.dateTo(), request.dateFrom())).thenReturn(List.of());
            when(bookingRepository.hasOverlappingBookings(eq(10L), eq(request.dateFrom()), eq(request.dateTo()), anyList()))
                    .thenReturn(false);
            when(bookingRepository.saveAndFlush(any(Booking.class))).thenReturn(booking);
            when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

            BookingDto result = bookingService.createBooking(request);
            ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

            assertThat(result.id()).isEqualTo(50L);
            verify(bookingValidator).validateCreateBookingRequest(request);
            verify(bookingRepository).saveAndFlush(bookingCaptor.capture());
            assertThat(bookingCaptor.getValue().getStatus()).isEqualTo(BookingStatus.CREATED);
            assertThat(bookingCaptor.getValue().getTotalPrice()).isEqualByComparingTo("4000");
        }

        @Test
        void shouldThrowEntityNotFound_whenPropertyMissing() {
            when(authService.getCurrentUser()).thenReturn(tenantUser);
            when(propertyRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Property not found");
        }

        @Test
        void shouldThrowIllegalState_whenPropertyIsNotActive() {
            property.setStatus(PropertyStatus.INACTIVE);
            when(authService.getCurrentUser()).thenReturn(tenantUser);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Only active properties can be booked.");
        }

        @Test
        void shouldThrowIllegalState_whenPropertyIsNotShortTerm() {
            property.setRentalType(RentalType.LONG_TERM);
            when(authService.getCurrentUser()).thenReturn(tenantUser);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Only short-term properties can be booked.");
        }

        @Test
        void shouldThrowIllegalArgument_whenTenantBooksOwnProperty() {
            property.setHost(tenantUser);
            when(authService.getCurrentUser()).thenReturn(tenantUser);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("You cannot book your own property.");
        }

        @Test
        void shouldThrowIllegalState_whenMaxGuestsNotConfigured() {
            property.setMaxGuests(null);
            when(authService.getCurrentUser()).thenReturn(tenantUser);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Property configuration is invalid: maxGuests is not set.");
        }

        @Test
        void shouldThrowIllegalArgument_whenGuestsExceedCapacity() {
            BookingRequestDto tooManyGuestsRequest = new BookingRequestDto(10L, request.dateFrom(), request.dateTo(), (short) 4);
            when(authService.getCurrentUser()).thenReturn(tenantUser);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));

            assertThatThrownBy(() -> bookingService.createBooking(tooManyGuestsRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Guest count exceeds the maximum capacity");
        }

        @Test
        void shouldThrowIllegalState_whenDatesBlockedByHost() {
            AvailabilityBlock block = AvailabilityBlock.builder().id(1L).property(property).build();
            when(authService.getCurrentUser()).thenReturn(tenantUser);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(availabilityRepository.findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(
                    10L, request.dateTo(), request.dateFrom())).thenReturn(List.of(block));

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("The property is blocked by the host for the selected dates.");
        }

        @Test
        void shouldThrowIllegalState_whenPropertyAlreadyBookedForDates() {
            when(authService.getCurrentUser()).thenReturn(tenantUser);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(availabilityRepository.findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(
                    10L, request.dateTo(), request.dateFrom())).thenReturn(List.of());
            when(bookingRepository.hasOverlappingBookings(eq(10L), eq(request.dateFrom()), eq(request.dateTo()), anyList()))
                    .thenReturn(true);

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("The property is already booked for the selected dates.");
        }

        @Test
        void shouldThrowIllegalState_whenPricePerNightMissing() {
            property.setPricing(null);
            when(authService.getCurrentUser()).thenReturn(tenantUser);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(availabilityRepository.findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(
                    10L, request.dateTo(), request.dateFrom())).thenReturn(List.of());
            when(bookingRepository.hasOverlappingBookings(eq(10L), eq(request.dateFrom()), eq(request.dateTo()), anyList()))
                    .thenReturn(false);

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Property configuration is invalid: pricePerNight is not set.");
        }

        @Test
        void shouldThrowIllegalState_whenConcurrentBookingHappens() {
            when(authService.getCurrentUser()).thenReturn(tenantUser);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(availabilityRepository.findAllByPropertyIdAndDateFromLessThanEqualAndDateToGreaterThanEqual(
                    10L, request.dateTo(), request.dateFrom())).thenReturn(List.of());
            when(bookingRepository.hasOverlappingBookings(eq(10L), eq(request.dateFrom()), eq(request.dateTo()), anyList()))
                    .thenReturn(false);
            when(bookingRepository.saveAndFlush(any(Booking.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

            assertThatThrownBy(() -> bookingService.createBooking(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Race condition: The property was just booked by someone else");
        }
    }

    @Nested
    @DisplayName("getBookingById()")
    class GetBookingByIdTests {

        @Test
        void shouldReturnBooking_whenCurrentUserIsTenant() {
            when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
            when(authService.getCurrentUser()).thenReturn(tenantUser);
            when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

            BookingDto result = bookingService.getBookingById(50L);

            assertThat(result.id()).isEqualTo(50L);
        }

        @Test
        void shouldReturnBooking_whenCurrentUserIsAdmin() {
            when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
            when(authService.getCurrentUser()).thenReturn(adminUser);
            when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

            BookingDto result = bookingService.getBookingById(50L);

            assertThat(result.id()).isEqualTo(50L);
        }

        @Test
        void shouldThrowAccessDenied_whenCurrentUserHasNoAccess() {
            when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
            when(authService.getCurrentUser()).thenReturn(outsiderUser);

            assertThatThrownBy(() -> bookingService.getBookingById(50L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You do not have permission to view this booking");
        }
    }

    @Nested
    @DisplayName("getMyBookings()")
    class GetMyBookingsTests {

        @Test
        void shouldReturnCurrentTenantBookings() {
            Page<Booking> bookingPage = new PageImpl<>(List.of(booking), pageable, 1);
            when(authService.getCurrentUser()).thenReturn(tenantUser);
            when(bookingRepository.findAllByTenantId(2L, pageable)).thenReturn(bookingPage);
            when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

            Page<BookingDto> result = bookingService.getMyBookings(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(50L);
        }
    }

    @Nested
    @DisplayName("cancelBooking()")
    class CancelBookingTests {

        @Test
        void shouldThrowEntityNotFound_whenBookingMissing() {
            when(bookingRepository.findById(77L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.cancelBooking(77L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Booking not found");
        }

        @Test
        void shouldThrowAccessDenied_whenUserIsNotTenantOrHost() {
            when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
            when(authService.getCurrentUser()).thenReturn(outsiderUser);

            assertThatThrownBy(() -> bookingService.cancelBooking(50L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You can only cancel your own or hosted bookings");
        }

        @Test
        void shouldThrowIllegalState_whenBookingAlreadyClosed() {
            booking.setStatus(BookingStatus.CANCELLED);
            when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
            when(authService.getCurrentUser()).thenReturn(tenantUser);

            assertThatThrownBy(() -> bookingService.cancelBooking(50L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Booking is already closed");
        }

        @Test
        void shouldThrowIllegalState_whenBookingCompleted() {
            booking.setStatus(BookingStatus.COMPLETED);
            when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
            when(authService.getCurrentUser()).thenReturn(tenantUser);

            assertThatThrownBy(() -> bookingService.cancelBooking(50L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Completed booking cannot be cancelled");
        }

        @Test
        void shouldThrowIllegalState_whenTenantCancelsInProgressBooking() {
            booking.setStatus(BookingStatus.IN_PROGRESS);
            when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
            when(authService.getCurrentUser()).thenReturn(tenantUser);

            assertThatThrownBy(() -> bookingService.cancelBooking(50L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Tenant cannot cancel booking in progress");
        }

        @Test
        void shouldCancelBookingWithoutRefund_whenNoPaidPaymentExists() {
            when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
            when(authService.getCurrentUser()).thenReturn(tenantUser);
            when(bookingRepository.save(booking)).thenReturn(booking);
            when(paymentRepository.existsByBookingIdAndStatus(50L, PaymentStatus.PAID)).thenReturn(false);
            when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

            BookingDto result = bookingService.cancelBooking(50L);

            assertThat(result.id()).isEqualTo(50L);
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        void shouldCancelBookingAndCreateRefund_whenPaidAndNotRefunded() {
            property.getPricing().setCurrency(" USD ");
            when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
            when(authService.getCurrentUser()).thenReturn(hostUser);
            when(bookingRepository.save(booking)).thenReturn(booking);
            when(paymentRepository.existsByBookingIdAndStatus(50L, PaymentStatus.PAID)).thenReturn(true);
            when(paymentRepository.existsByBookingIdAndStatus(50L, PaymentStatus.REFUNDED)).thenReturn(false);
            when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

            bookingService.cancelBooking(50L);
            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

            verify(paymentRepository).save(paymentCaptor.capture());
            assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(paymentCaptor.getValue().getCurrency()).isEqualTo("USD");
            assertThat(paymentCaptor.getValue().getProvider()).isEqualTo("MOCK_GATEWAY");
        }
    }

    @Nested
    @DisplayName("getIncomingBookings()")
    class GetIncomingBookingsTests {

        @Test
        void shouldReturnHostIncomingBookings() {
            Page<Booking> bookingPage = new PageImpl<>(List.of(booking), pageable, 1);
            when(authService.getCurrentUser()).thenReturn(hostUser);
            when(bookingRepository.findAllByPropertyHostId(1L, pageable)).thenReturn(bookingPage);
            when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

            Page<BookingDto> result = bookingService.getIncomingBookings(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(50L);
        }
    }

    @Nested
    @DisplayName("confirmBooking()/rejectBooking()")
    class HostActionTests {

        @Test
        void shouldConfirmBooking_whenHostActsOnCreatedBooking() {
            when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
            when(authService.getCurrentUser()).thenReturn(hostUser);
            when(bookingRepository.save(booking)).thenReturn(booking);
            when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

            bookingService.confirmBooking(50L);

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
            verify(bookingRepository).save(booking);
        }

        @Test
        void shouldRejectBooking_whenHostActsOnCreatedBooking() {
            when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
            when(authService.getCurrentUser()).thenReturn(hostUser);
            when(bookingRepository.save(booking)).thenReturn(booking);
            when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

            bookingService.rejectBooking(50L);

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.REJECTED);
            verify(bookingRepository).save(booking);
        }

        @Test
        void shouldThrowForbidden_whenNonHostAttemptsHostAction() {
            when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
            when(authService.getCurrentUser()).thenReturn(tenantUser);

            assertThatThrownBy(() -> bookingService.confirmBooking(50L))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
        }

        @Test
        void shouldThrowIllegalState_whenBookingIsNotCreated() {
            booking.setStatus(BookingStatus.CONFIRMED);
            when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
            when(authService.getCurrentUser()).thenReturn(hostUser);

            assertThatThrownBy(() -> bookingService.confirmBooking(50L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("You can only change the status of CREATED bookings");
        }
    }
}
