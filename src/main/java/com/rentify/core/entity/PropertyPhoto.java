package com.rentify.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

@Entity
@Table(
        name = "property_photos",
        indexes = @Index(name = "idx_property_photos_property_id", columnList = "property_id")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyPhoto extends CreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false, length = 800)
    private String url;

    @Column(name = "cloudinary_public_id", length = 255)
    private String cloudinaryPublicId;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        PropertyPhoto that = (PropertyPhoto) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
