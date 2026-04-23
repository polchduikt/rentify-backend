package com.rentify.core.entity;

import com.rentify.core.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_booking_id", columnList = "booking_id"),
                @Index(name = "idx_payments_booking_created_at", columnList = "booking_id, created_at")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Payment extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @NotNull
    private Booking booking;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @NotBlank
    @Size(min = 3, max = 3)
    @Column(nullable = false, length = 3)
    private String currency = "UAH";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(length = 60)
    private String provider;

    @Column(name = "provider_payment_id", length = 120)
    private String providerPaymentId;
}
