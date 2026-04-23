package com.rentify.core.entity;

import com.rentify.core.enums.AmenityCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "amenities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Amenity extends BaseEntity {

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private AmenityCategory category;

    @Size(max = 120)
    @Column(unique = true, length = 120)
    private String slug;

    @Size(max = 120)
    @Column(name = "icon", length = 120)
    private String icon;
}
