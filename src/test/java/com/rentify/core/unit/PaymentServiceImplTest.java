package com.rentify.core.unit;

import com.rentify.core.dto.payment.PaymentResponseDto;
import com.rentify.core.entity.Booking;
import com.rentify.core.entity.Payment;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.PropertyPricing;
import com.rentify.core.entity.Role;
import com.rentify.core.entity.User;
import com.rentify.core.enums.BookingStatus;
import com.rentify.core.enums.PaymentStatus;
import com.rentify.core.mapper.PaymentMapper;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.PaymentRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.CurrencyResolver;
import com.rentify.core.service.impl.PaymentServiceImpl;
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
import org.springframework.security.access.AccessDeniedException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private AuthenticationService authenticationService;
    @Mock private PaymentMapper paymentMapper;
    @Mock private CurrencyResolver currencyResolver;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User tenant;
    private User host;
    private User outsider;
    private User admin;
    private Booking booking;

    @BeforeEach
    void setUp() {
        Role userRole = Role.builder().name("ROLE_USER").build();
        Role adminRole = Role.builder().name("ROLE_ADMIN").build();
        tenant = User.builder().id(1L).roles(Set.of(userRole)).build();
        host = User.builder().id(2L).roles(Set.of(userRole)).build();
        outsider = User.builder().id(3L).roles(Set.of(userRole)).build();
        admin = User.builder().id(4L).roles(Set.of(adminRole)).build();

        PropertyPricing pricing = PropertyPricing.builder().currency("UAH").build();
        Property property = Property.builder().id(10L).host(host).pricing(pricing).build();
        pricing.setProperty(property);

        booking = Booking.builder()
                .id(100L)
                .property(property)
                .tenant(tenant)
                .status(BookingStatus.CONFIRMED)
                .totalPrice(new BigDecimal("2500.00"))
                .build();
        lenient().when(currencyResolver.resolvePropertyCurrency(property)).thenReturn("UAH");
    }

    @Nested
    @DisplayName("payBooking()")
    class PayBookingTests {

        @Test
        void shouldThrowEntityNotFound_whenBookingMissing() {
            when(bookingRepository.findById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.payBooking(100L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Booking not found");
        }

        @Test
        void shouldThrowAccessDenied_whenCurrentUserIsNotTenant() {
            when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
            when(authenticationService.getCurrentUser()).thenReturn(outsider);

            assertThatThrownBy(() -> paymentService.payBooking(100L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You can only pay for your own bookings");
        }

        @Test
        void shouldThrowIllegalState_whenAlreadyPaid() {
            when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
            when(authenticationService.getCurrentUser()).thenReturn(tenant);
            when(paymentRepository.existsByBookingIdAndStatus(100L, PaymentStatus.PAID)).thenReturn(true);

            assertThatThrownBy(() -> paymentService.payBooking(100L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Booking is already paid");
        }

        @Test
        void shouldThrowIllegalState_whenBookingNotConfirmed() {
            booking.setStatus(BookingStatus.CREATED);
            when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
            when(authenticationService.getCurrentUser()).thenReturn(tenant);
            when(paymentRepository.existsByBookingIdAndStatus(100L, PaymentStatus.PAID)).thenReturn(false);

            assertThatThrownBy(() -> paymentService.payBooking(100L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Booking must be CONFIRMED by host before payment");
        }

        @Test
        void shouldThrowIllegalState_whenTotalPriceMissing() {
            booking.setTotalPrice(null);
            when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
            when(authenticationService.getCurrentUser()).thenReturn(tenant);
            when(paymentRepository.existsByBookingIdAndStatus(100L, PaymentStatus.PAID)).thenReturn(false);

            assertThatThrownBy(() -> paymentService.payBooking(100L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Booking has no calculated total price");
        }

        @Test
        void shouldCreatePaidPayment_whenBookingCanBePaid() {
            booking.getProperty().getPricing().setCurrency(" USD ");
            when(currencyResolver.resolvePropertyCurrency(booking.getProperty())).thenReturn("USD");
            Payment savedPayment = Payment.builder()
                    .id(500L)
                    .booking(booking)
                    .amount(booking.getTotalPrice())
                    .currency("USD")
                    .status(PaymentStatus.PAID)
                    .provider("MOCK_GATEWAY")
                    .providerPaymentId("mock_id")
                    .build();
            PaymentResponseDto responseDto = new PaymentResponseDto(
                    500L,
                    100L,
                    new BigDecimal("2500.00"),
                    "USD",
                    PaymentStatus.PAID,
                    "MOCK_GATEWAY",
                    "mock_id",
                    BookingStatus.CONFIRMED,
                    ZonedDateTime.now(),
                    ZonedDateTime.now()
            );

            when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
            when(authenticationService.getCurrentUser()).thenReturn(tenant);
            when(paymentRepository.existsByBookingIdAndStatus(100L, PaymentStatus.PAID)).thenReturn(false);
            when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
            when(paymentMapper.toDto(savedPayment)).thenReturn(responseDto);

            PaymentResponseDto result = paymentService.payBooking(100L);
            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

            assertThat(result.id()).isEqualTo(500L);
            verify(paymentRepository).save(paymentCaptor.capture());
            assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.PAID);
            assertThat(paymentCaptor.getValue().getCurrency()).isEqualTo("USD");
            assertThat(paymentCaptor.getValue().getProvider()).isEqualTo("MOCK_GATEWAY");
            assertThat(paymentCaptor.getValue().getProviderPaymentId()).startsWith("mock_");
        }
    }

    @Nested
    @DisplayName("getMyPayments()")
    class GetMyPaymentsTests {

        @Test
        void shouldReturnMappedPaymentsForCurrentUser() {
            Payment payment = Payment.builder().id(1L).booking(booking).build();
            PaymentResponseDto dto = new PaymentResponseDto(
                    1L, 100L, new BigDecimal("2500.00"), "UAH",
                    PaymentStatus.PAID, "MOCK_GATEWAY", "mock_1",
                    BookingStatus.CONFIRMED, ZonedDateTime.now(), ZonedDateTime.now()
            );
            when(authenticationService.getCurrentUser()).thenReturn(tenant);
            when(paymentRepository.findAllByBookingTenantIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(payment));
            when(paymentMapper.toDtos(List.of(payment))).thenReturn(List.of(dto));

            List<PaymentResponseDto> result = paymentService.getMyPayments();

            assertThat(result).containsExactly(dto);
        }
    }

    @Nested
    @DisplayName("getPaymentsByBooking()")
    class GetPaymentsByBookingTests {

        @Test
        void shouldThrowEntityNotFound_whenBookingMissing() {
            when(bookingRepository.findById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getPaymentsByBooking(100L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Booking not found");
        }

        @Test
        void shouldThrowAccessDenied_whenUserHasNoAccessToBooking() {
            when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
            when(authenticationService.getCurrentUser()).thenReturn(outsider);

            assertThatThrownBy(() -> paymentService.getPaymentsByBooking(100L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You do not have permission to view these payments");
        }

        @Test
        void shouldReturnPayments_whenCurrentUserIsAdmin() {
            Payment payment = Payment.builder().id(2L).booking(booking).status(PaymentStatus.PAID).build();
            PaymentResponseDto dto = new PaymentResponseDto(
                    2L, 100L, new BigDecimal("2500.00"), "UAH",
                    PaymentStatus.PAID, "MOCK_GATEWAY", "mock_2",
                    BookingStatus.CONFIRMED, ZonedDateTime.now(), ZonedDateTime.now()
            );

            when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
            when(authenticationService.getCurrentUser()).thenReturn(admin);
            when(paymentRepository.findAllByBookingIdOrderByCreatedAtDesc(100L)).thenReturn(List.of(payment));
            when(paymentMapper.toDtos(List.of(payment))).thenReturn(List.of(dto));

            List<PaymentResponseDto> result = paymentService.getPaymentsByBooking(100L);

            assertThat(result).containsExactly(dto);
        }
    }
}
