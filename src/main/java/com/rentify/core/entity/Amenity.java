package com.rentify.core.entity;

import com.rentify.core.enums.AmenityCategory;
import jakarta.persistence.*;
import lombok.*;

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
}
