package com.rentify.core.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "property_rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false, unique = true)
    private Property property;

    @Builder.Default
    @Column(name = "pets_allowed", nullable = false)
    private Boolean petsAllowed = false;

    @Builder.Default
    @Column(name = "smoking_allowed", nullable = false)
    private Boolean smokingAllowed = false;

    @Builder.Default
    @Column(name = "parties_allowed", nullable = false)
    private Boolean partiesAllowed = false;

    @Column(name = "additional_rules", columnDefinition = "TEXT")
    private String additionalRules;
}
