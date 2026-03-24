package com.rentify.core.entity;

import jakarta.persistence.*;
import lombok.*;

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
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review extends CreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private Short rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

}
