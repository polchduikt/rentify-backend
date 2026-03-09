package com.rentify.core.controller;

import com.rentify.core.dto.payment.PaymentResponseDto;
import com.rentify.core.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/bookings/{bookingId}/mock-pay")
    public ResponseEntity<PaymentResponseDto> mockPayBooking(@PathVariable Long bookingId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.PayBooking(bookingId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponseDto>> getMyPayments() {
        return ResponseEntity.ok(paymentService.getMyPayments());
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentsByBooking(bookingId));
    }
}
