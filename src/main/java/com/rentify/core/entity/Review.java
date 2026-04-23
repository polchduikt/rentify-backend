package com.rentify.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "reviews",
        indexes = {
                @Index(name = "idx_reviews_property_id", columnList = "property_id"),
                @Index(name = "idx_reviews_author_id", columnList = "author_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reviews_booking_author",
                columnNames = {"booking_id", "author_id"}
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Review extends CreatedAtEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @NotNull
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull
    private User author;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Short rating;

    @Column(columnDefinition = "TEXT")
    private String comment;
}
