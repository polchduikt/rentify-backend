package com.rentify.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

@Entity
@Table(name = "property_rules")
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "created_at", nullable = true)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "updated_at", nullable = true))
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyRule extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false, unique = true)
    @NotNull
    private Property property;

    @Builder.Default
    @Column(name = "pets_allowed", nullable = false)
    @NotNull
    private Boolean petsAllowed = false;

    @Builder.Default
    @Column(name = "smoking_allowed", nullable = false)
    @NotNull
    private Boolean smokingAllowed = false;

    @Builder.Default
    @Column(name = "parties_allowed", nullable = false)
    @NotNull
    private Boolean partiesAllowed = false;

    @Column(name = "additional_rules", columnDefinition = "TEXT")
    private String additionalRules;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        PropertyRule that = (PropertyRule) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
