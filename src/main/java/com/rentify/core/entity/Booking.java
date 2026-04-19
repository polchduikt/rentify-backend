package com.rentify.core.entity;

import com.rentify.core.enums.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.Hibernate;
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
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    @Column(name = "date_from", nullable = false)
    private LocalDate dateFrom;

    @Column(name = "date_to", nullable = false)
    private LocalDate dateTo;

    @Builder.Default
    @Column(nullable = false)
    @Min(1)
    private Short guests = 1;

    @Column(name = "total_price", precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CREATED;

    @Builder.Default
    @Version
    @Column(name = "version")
    private long version = 0L;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Booking booking = (Booking) o;
        return id != null && id.equals(booking.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
