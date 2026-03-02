package com.rentify.core.repository;

import com.rentify.core.entity.PropertyPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyPricingRepository extends JpaRepository<PropertyPricing, Long> {
    Optional<PropertyPricing> findByPropertyId(Long propertyId);
}
