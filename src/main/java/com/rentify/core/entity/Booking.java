package com.rentify.core.entity;

import com.rentify.core.enums.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "bookings",
        indexes = {
                @Index(name = "idx_bookings_tenant_created_at", columnList = "tenant_id, created_at"),
                @Index(name = "idx_bookings_tenant_property_status", columnList = "tenant_id, property_id, status"),
                @Index(name = "idx_bookings_property_status_dates", columnList = "property_id, status, date_from, date_to"),
                @Index(name = "idx_bookings_status_date_from", columnList = "status, date_from"),
                @Index(name = "idx_bookings_status_date_to", columnList = "status, date_to")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Booking extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @NotNull
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull
    private User tenant;

    @NotNull
    @Column(name = "date_from", nullable = false)
    private LocalDate dateFrom;

    @NotNull
    @Column(name = "date_to", nullable = false)
    private LocalDate dateTo;

    @Builder.Default
    @Column(nullable = false)
    @NotNull
    @Min(1)
    private Short guests = 1;

    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private BookingStatus status = BookingStatus.CREATED;

    @Builder.Default
    @Version
    @Column(name = "version")
    private long version = 0L;

    @AssertTrue(message = "dateFrom must be on or before dateTo")
    public boolean isDateRangeValid() {
        if (dateFrom == null || dateTo == null) {
            return true;
        }
        return !dateFrom.isAfter(dateTo);
    }
}
