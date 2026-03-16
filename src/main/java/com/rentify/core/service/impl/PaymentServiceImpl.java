package com.rentify.core.service.impl;

import com.rentify.core.dto.payment.PaymentResponseDto;
import com.rentify.core.entity.Booking;
import com.rentify.core.entity.Payment;
import com.rentify.core.entity.User;
import com.rentify.core.enums.BookingStatus;
import com.rentify.core.enums.PaymentStatus;
import com.rentify.core.mapper.PaymentMapper;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.PaymentRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String MOCK_PROVIDER = "MOCK_GATEWAY";

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final AuthenticationService authenticationService;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponseDto payBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        User currentUser = authenticationService.getCurrentUser();

        if (!booking.getTenant().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only pay for your own bookings");
        }
        if (paymentRepository.existsByBookingIdAndStatus(bookingId, PaymentStatus.PAID)) {
            throw new IllegalStateException("Booking is already paid");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Booking must be CONFIRMED by host before payment");
        }
        if (booking.getTotalPrice() == null) {
            throw new IllegalStateException("Booking has no calculated total price");
        }

        String currency = "UAH";
        if (booking.getProperty().getPricing() != null
                && booking.getProperty().getPricing().getCurrency() != null
                && !booking.getProperty().getPricing().getCurrency().isBlank()) {
            currency = booking.getProperty().getPricing().getCurrency().trim();
        }

        // TODO: Replace mock payment flow with a real payment gateway integration (LiqPay/Stripe/etc.).
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalPrice())
                .currency(currency)
                .status(PaymentStatus.PAID)
                .provider(MOCK_PROVIDER)
                .providerPaymentId("mock_" + UUID.randomUUID())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getMyPayments() {
        User currentUser = authenticationService.getCurrentUser();
        return paymentRepository.findAllByBookingTenantIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        User currentUser = authenticationService.getCurrentUser();

        boolean isTenant = booking.getTenant().getId().equals(currentUser.getId());
        boolean isHost = booking.getProperty().getHost().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));

        if (!isTenant && !isHost && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to view these payments");
        }

        return paymentRepository.findAllByBookingIdOrderByCreatedAtDesc(bookingId)
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }
}
