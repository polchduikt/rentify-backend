package com.rentify.core.entity;

import com.rentify.core.enums.AmenityCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

@Entity
@Table(name = "amenities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private AmenityCategory category;

    @Column(unique = true, length = 120)
    private String slug;

    @Column(name = "icon", length = 120)
    private String icon;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Amenity amenity = (Amenity) o;
        return id != null && id.equals(amenity.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
