package com.rentify.core.entity;

import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.PropertyMarketType;
import com.rentify.core.enums.PropertyType;
import com.rentify.core.enums.RentalType;
import com.rentify.core.converter.PropertyTypeConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
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
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Property extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    @NotNull
    private User host;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_type", nullable = false)
    @NotNull
    private RentalType rentalType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private PropertyStatus status = PropertyStatus.DRAFT;

    @Convert(converter = PropertyTypeConverter.class)
    @Column(name = "property_type", length = 60)
    private PropertyType propertyType;

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
    @NotNull
    private Boolean isTopPromoted = false;

    @Builder.Default
    @Column(name = "view_count", nullable = false, columnDefinition = "bigint default 0")
    @NotNull
    @Min(0)
    private Long viewCount = 0L;

    @Builder.Default
    @Column(name = "review_count", nullable = false, columnDefinition = "bigint default 0")
    @NotNull
    @Min(0)
    private Long reviewCount = 0L;

    @Builder.Default
    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2, columnDefinition = "numeric(3,2) default 0")
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "top_promoted_until")
    private ZonedDateTime topPromotedUntil;

    @Min(0)
    private Short rooms;
    @Min(0)
    private Short floor;
    @Column(name = "total_floors")
    @Min(0)
    private Short totalFloors;

    @DecimalMin("0.00")
    @Column(name = "area_sqm", precision = 7, scale = 2)
    private BigDecimal areaSqm;

    @Column(name = "max_guests")
    @Min(0)
    private Short maxGuests;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private java.util.List<PropertyPhoto> photos = new java.util.ArrayList<>();

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", unique = true)
    private Address address;

    @ManyToMany(fetch = FetchType.LAZY)
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

    public BigDecimal resolveMapPrice() {
        if (pricing == null) {
            return null;
        }
        if (rentalType == RentalType.SHORT_TERM) {
            return pricing.getPricePerNight() != null ? pricing.getPricePerNight() : pricing.getPricePerMonth();
        }
        if (rentalType == RentalType.LONG_TERM) {
            return pricing.getPricePerMonth() != null ? pricing.getPricePerMonth() : pricing.getPricePerNight();
        }
        return pricing.getPricePerNight() != null ? pricing.getPricePerNight() : pricing.getPricePerMonth();
    }
}
