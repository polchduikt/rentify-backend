package com.rentify.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "property_rules")
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "created_at", nullable = true)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "updated_at", nullable = true))
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class PropertyRule extends AuditableEntity {

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
}
