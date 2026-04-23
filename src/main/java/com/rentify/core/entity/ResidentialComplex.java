package com.rentify.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "residential_complexes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_residential_complex_city_normalized",
                columnNames = {"city_id", "normalized_name"}
        ),
        indexes = {
                @Index(name = "idx_residential_complex_city_id", columnList = "city_id"),
                @Index(name = "idx_residential_complex_name", columnList = "name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ResidentialComplex extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    @NotNull
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @NotBlank
    @Size(max = 220)
    @Column(nullable = false, length = 220)
    private String name;

    @NotBlank
    @Size(max = 220)
    @Column(name = "normalized_name", nullable = false, length = 220)
    private String normalizedName;
}
