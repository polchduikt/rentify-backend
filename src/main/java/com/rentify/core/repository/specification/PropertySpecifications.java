package com.rentify.core.repository.specification;

import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import com.rentify.core.entity.Property;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class PropertySpecifications {

    public static Specification<Property> withFilters(PropertySearchCriteriaDto criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), com.rentify.core.enums.PropertyStatus.ACTIVE));
            if (criteria.city() != null && !criteria.city().isBlank()) {
                predicates.add(cb.equal(root.get("address").get("location").get("city"), criteria.city()));
            }
            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pricing").get("pricePerNight"), criteria.minPrice()));
            }
            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pricing").get("pricePerNight"), criteria.maxPrice()));
            }
            if (criteria.minRooms() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rooms"), criteria.minRooms()));
            }
            if (criteria.maxRooms() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rooms"), criteria.maxRooms()));
            }
            if (criteria.minFloor() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("floor"), criteria.minFloor()));
            }
            if (criteria.maxFloor() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("floor"), criteria.maxFloor()));
            }
            if (criteria.minArea() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("areaSqm"), criteria.minArea()));
            }
            if (criteria.maxArea() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("areaSqm"), criteria.maxArea()));
            }
            if (criteria.rentalType() != null) {
                predicates.add(cb.equal(root.get("rentalType"), criteria.rentalType()));
            }
            if (criteria.petsAllowed() != null) {
                predicates.add(cb.equal(root.get("rules").get("petsAllowed"), criteria.petsAllowed()));
            }
            if (criteria.amenityIds() != null && !criteria.amenityIds().isEmpty()) {
                Join<Object, Object> amenitiesJoin = root.join("amenities");
                predicates.add(amenitiesJoin.get("id").in(criteria.amenityIds()));
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
