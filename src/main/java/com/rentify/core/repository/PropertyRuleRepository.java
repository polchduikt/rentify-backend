package com.rentify.core.repository;

import com.rentify.core.entity.PropertyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyRuleRepository extends JpaRepository<PropertyRule, Long> {
    Optional<PropertyRule> findByPropertyId(Long propertyId);
}