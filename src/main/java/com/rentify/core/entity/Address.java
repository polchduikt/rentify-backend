package com.rentify.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import java.math.BigDecimal;

@Entity
@Table(name = "addresses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City cityRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District districtRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metro_station_id")
    private MetroStation metroStationRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "residential_complex_id")
    private ResidentialComplex residentialComplexRef;

    @Column(length = 180)
    private String street;

    @Column(name = "house_number", length = 30)
    private String houseNumber;

    @Column(length = 30)
    private String apartment;

    @Column(name = "postal_code", length = 30)
    private String postalCode;

    @Column(precision = 9, scale = 6)
    private BigDecimal lat;

    @Column(precision = 9, scale = 6)
    private BigDecimal lng;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Address address = (Address) o;
        return id != null && id.equals(address.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
