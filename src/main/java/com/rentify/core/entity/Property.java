package com.rentify.core.entity;

import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.PropertyMarketType;
import com.rentify.core.enums.RentalType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Set;

@Entity
@Table(
        name = "properties",
        indexes = {
                @Index(name = "idx_properties_host_created_at", columnList = "host_id, created_at"),
                @Index(name = "idx_properties_host_status", columnList = "host_id, status"),
                @Index(name = "idx_properties_status_created_at", columnList = "status, created_at"),
                @Index(name = "idx_properties_rental_type", columnList = "rental_type"),
                @Index(name = "idx_properties_top_promoted_until", columnList = "is_top_promoted, top_promoted_until")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Property extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_type", nullable = false)
    private RentalType rentalType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus status = PropertyStatus.DRAFT;

    @Column(name = "property_type", length = 60)
    private String propertyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "market_type", length = 40)
    private PropertyMarketType marketType;

    @Builder.Default
    @Column(name = "is_verified_property")
    private Boolean isVerifiedProperty = false;

    @Builder.Default
    @Column(name = "is_verified_realtor")
    private Boolean isVerifiedRealtor = false;

    @Builder.Default
    @Column(name = "is_duplicate")
    private Boolean isDuplicate = false;

    @Builder.Default
    @Column(name = "is_top_promoted", nullable = false, columnDefinition = "boolean default false")
    private Boolean isTopPromoted = false;

    @Builder.Default
    @Column(name = "view_count", nullable = false, columnDefinition = "bigint default 0")
    private Long viewCount = 0L;

    @Builder.Default
    @Column(name = "review_count", nullable = false, columnDefinition = "bigint default 0")
    private Long reviewCount = 0L;

    @Builder.Default
    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2, columnDefinition = "numeric(3,2) default 0")
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "top_promoted_until")
    private ZonedDateTime topPromotedUntil;

    private Short rooms;
    private Short floor;
    @Column(name = "total_floors")
    private Short totalFloors;

    @Column(name = "area_sqm", precision = 7, scale = 2)
    private BigDecimal areaSqm;

    @Column(name = "max_guests")
    private Short maxGuests;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private java.util.List<PropertyPhoto> photos = new java.util.ArrayList<>();

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @ManyToMany
    @JoinTable(
            name = "property_amenities",
            joinColumns = @JoinColumn(name = "property_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    @Builder.Default
    private Set<Amenity> amenities = new java.util.HashSet<>();

    @OneToOne(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PropertyPricing pricing;

    @OneToOne(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PropertyRule rules;
}
