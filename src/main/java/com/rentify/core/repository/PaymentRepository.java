package com.rentify.core.repository;

import com.rentify.core.entity.Payment;
import com.rentify.core.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByBookingId(Long bookingId);
    List<Payment> findAllByBookingIdOrderByCreatedAtDesc(Long bookingId);
    List<Payment> findAllByBookingTenantIdOrderByCreatedAtDesc(Long tenantId);
    boolean existsByBookingIdAndStatus(Long bookingId, PaymentStatus status);
}
