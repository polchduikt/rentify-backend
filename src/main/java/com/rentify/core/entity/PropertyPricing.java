package com.rentify.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;
import java.math.BigDecimal;

@Entity
@Table(name = "property_pricing")
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "created_at", nullable = true)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "updated_at", nullable = true))
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyPricing extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false, unique = true)
    @NotNull
    private Property property;

    @Column(name = "price_per_night", precision = 12, scale = 2)
    private BigDecimal pricePerNight;

    @Column(name = "price_per_month", precision = 12, scale = 2)
    private BigDecimal pricePerMonth;

    @Builder.Default
    @Column(nullable = false, length = 3)
    @NotBlank
    @Size(min = 3, max = 3)
    private String currency = "UAH";

    @Column(name = "security_deposit", precision = 12, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal securityDeposit;

    @Column(name = "cleaning_fee", precision = 12, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal cleaningFee;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        PropertyPricing that = (PropertyPricing) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
