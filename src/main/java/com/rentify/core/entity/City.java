package com.rentify.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

@Entity
@Table(
        name = "cities",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_city_normalized_name", columnNames = "normalized_name"),
                @UniqueConstraint(name = "uk_city_kato_code", columnNames = "kato_code")
        },
        indexes = {
                @Index(name = "idx_city_name", columnList = "name"),
                @Index(name = "idx_city_normalized_name", columnList = "normalized_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank
    @Size(max = 120)
    @Column(name = "normalized_name", nullable = false, length = 120)
    private String normalizedName;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String country;

    @Size(max = 120)
    @Column(length = 120)
    private String region;

    @Size(max = 32)
    @Column(name = "kato_code", length = 32)
    private String katoCode;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        City that = (City) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
