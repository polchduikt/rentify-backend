package com.rentify.core.service;

import com.rentify.core.dto.payment.PaymentResponseDto;

import java.util.List;

public interface PaymentService {
    PaymentResponseDto payBooking(Long bookingId);
    List<PaymentResponseDto> getMyPayments();
    List<PaymentResponseDto> getPaymentsByBooking(Long bookingId);
}
