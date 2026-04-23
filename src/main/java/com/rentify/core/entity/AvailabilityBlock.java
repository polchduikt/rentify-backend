package com.rentify.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Check;
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
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class AvailabilityBlock extends CreatedAtEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @NotNull
    private Property property;

    @NotNull
    @Column(name = "date_from", nullable = false)
    private LocalDate dateFrom;

    @NotNull
    @Column(name = "date_to", nullable = false)
    private LocalDate dateTo;

    @Column(length = 120)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @NotNull
    private User createdBy;
}
