package com.rentify.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.Hibernate;
import java.time.LocalDate;

@Entity
@Table(
        name = "availability_blocks",
        indexes = {
                @Index(name = "idx_availability_blocks_property_id", columnList = "property_id"),
                @Index(name = "idx_availability_blocks_property_dates", columnList = "property_id, date_from, date_to")
        }
)
@Check(name = "ck_availability_blocks_date_range", constraints = "date_from <= date_to")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AvailabilityBlock extends CreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "date_from", nullable = false)
    private LocalDate dateFrom;

    @Column(name = "date_to", nullable = false)
    private LocalDate dateTo;

    @Column(length = 120)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        AvailabilityBlock that = (AvailabilityBlock) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
