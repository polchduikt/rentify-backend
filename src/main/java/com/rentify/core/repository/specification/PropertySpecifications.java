package com.rentify.core.repository.specification;

import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import com.rentify.core.entity.AvailabilityBlock;
import com.rentify.core.entity.Amenity;
import com.rentify.core.entity.Booking;
import com.rentify.core.entity.Property;
import com.rentify.core.enums.BookingStatus;
import com.rentify.core.enums.RentalType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PropertySpecifications {

    public static Specification<Property> withFilters(PropertySearchCriteriaDto criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), com.rentify.core.enums.PropertyStatus.ACTIVE));
            if (criteria.propertyId() != null) {
                predicates.add(cb.equal(root.get("id"), criteria.propertyId()));
            }
            if (criteria.cityId() != null) {
                predicates.add(cb.equal(root.get("address").get("cityRef").get("id"), criteria.cityId()));
            }
            if (criteria.districtId() != null) {
                predicates.add(cb.equal(root.get("address").get("districtRef").get("id"), criteria.districtId()));
            }
            if (criteria.metroStationId() != null) {
                predicates.add(cb.equal(root.get("address").get("metroStationRef").get("id"), criteria.metroStationId()));
            }
            if (criteria.residentialComplexId() != null) {
                predicates.add(cb.equal(root.get("address").get("residentialComplexRef").get("id"), criteria.residentialComplexId()));
            }
            if (criteria.country() != null && !criteria.country().isBlank()) {
                predicates.add(cb.equal(root.get("address").get("location").get("country"), criteria.country()));
            }
            if (criteria.region() != null && !criteria.region().isBlank()) {
                predicates.add(cb.equal(root.get("address").get("location").get("region"), criteria.region()));
            }
            if (criteria.city() != null && !criteria.city().isBlank()) {
                predicates.add(cb.equal(root.get("address").get("location").get("city"), criteria.city()));
            }
            if (criteria.lat() != null && criteria.lng() != null && criteria.radiusKm() != null) {
                predicates.add(cb.isNotNull(root.get("address").get("lat")));
                predicates.add(cb.isNotNull(root.get("address").get("lng")));
                var lat1 = cb.function("radians", Double.class, cb.literal(criteria.lat()));
                var lng1 = cb.function("radians", Double.class, cb.literal(criteria.lng()));
                var lat2 = cb.function("radians", Double.class, root.get("address").get("lat").as(Double.class));
                var lng2 = cb.function("radians", Double.class, root.get("address").get("lng").as(Double.class));
                var sinPart = cb.prod(
                        cb.function("sin", Double.class, lat1),
                        cb.function("sin", Double.class, lat2)
                );
                var cosPart = cb.prod(
                        cb.prod(
                                cb.function("cos", Double.class, lat1),
                                cb.function("cos", Double.class, lat2)
                        ),
                        cb.function("cos", Double.class, cb.diff(lng2, lng1))
                );
                var distance = cb.prod(
                        cb.literal(6371.0),
                        cb.function("acos", Double.class, cb.sum(sinPart, cosPart))
                );
                predicates.add(cb.le(distance, criteria.radiusKm()));
            }
            if (criteria.southWestLat() != null
                    && criteria.southWestLng() != null
                    && criteria.northEastLat() != null
                    && criteria.northEastLng() != null) {
                predicates.add(cb.isNotNull(root.get("address").get("lat")));
                predicates.add(cb.isNotNull(root.get("address").get("lng")));
                predicates.add(cb.between(
                        root.get("address").get("lat").as(Double.class),
                        criteria.southWestLat(),
                        criteria.northEastLat()
                ));

                if (criteria.southWestLng() <= criteria.northEastLng()) {
                    predicates.add(cb.between(
                            root.get("address").get("lng").as(Double.class),
                            criteria.southWestLng(),
                            criteria.northEastLng()
                    ));
                } else {
                    predicates.add(cb.or(
                            cb.greaterThanOrEqualTo(root.get("address").get("lng").as(Double.class), criteria.southWestLng()),
                            cb.lessThanOrEqualTo(root.get("address").get("lng").as(Double.class), criteria.northEastLng())
                    ));
                }
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
            if (criteria.minTotalFloors() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalFloors"), criteria.minTotalFloors()));
            }
            if (criteria.maxTotalFloors() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalFloors"), criteria.maxTotalFloors()));
            }
            if (criteria.minSleepingPlaces() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxGuests"), criteria.minSleepingPlaces()));
            }
            if (criteria.maxSleepingPlaces() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("maxGuests"), criteria.maxSleepingPlaces()));
            }
            if (criteria.minArea() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("areaSqm"), criteria.minArea()));
            }
            if (criteria.maxArea() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("areaSqm"), criteria.maxArea()));
            }
            if (criteria.dateFrom() != null && criteria.dateTo() != null) {
                predicates.add(cb.equal(root.get("rentalType"), RentalType.SHORT_TERM));

                var blockSubquery = query.subquery(Long.class);
                var blockRoot = blockSubquery.from(AvailabilityBlock.class);
                blockSubquery.select(cb.literal(1L));
                blockSubquery.where(
                        cb.equal(blockRoot.get("property").get("id"), root.get("id")),
                        cb.lessThanOrEqualTo(blockRoot.get("dateFrom"), criteria.dateTo()),
                        cb.greaterThanOrEqualTo(blockRoot.get("dateTo"), criteria.dateFrom())
                );
                predicates.add(cb.not(cb.exists(blockSubquery)));

                var bookingSubquery = query.subquery(Long.class);
                var bookingRoot = bookingSubquery.from(Booking.class);
                bookingSubquery.select(cb.literal(1L));
                bookingSubquery.where(
                        cb.equal(bookingRoot.get("property").get("id"), root.get("id")),
                        cb.not(bookingRoot.get("status").in(List.of(BookingStatus.CANCELLED, BookingStatus.REJECTED))),
                        cb.lessThan(bookingRoot.get("dateFrom"), criteria.dateTo()),
                        cb.greaterThan(bookingRoot.get("dateTo"), criteria.dateFrom())
                );
                predicates.add(cb.not(cb.exists(bookingSubquery)));
            }
            if (criteria.rentalType() != null) {
                predicates.add(cb.equal(root.get("rentalType"), criteria.rentalType()));
            }
            if (criteria.marketType() != null) {
                predicates.add(cb.equal(root.get("marketType"), criteria.marketType()));
            }
            if (criteria.propertyType() != null && !criteria.propertyType().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("propertyType")),
                        criteria.propertyType().toLowerCase(Locale.ROOT)
                ));
            }
            if (criteria.verifiedProperty() != null) {
                if (criteria.verifiedProperty()) {
                    predicates.add(cb.isTrue(root.get("isVerifiedProperty").as(Boolean.class)));
                } else {
                    predicates.add(cb.or(
                            cb.isFalse(root.get("isVerifiedProperty").as(Boolean.class)),
                            cb.isNull(root.get("isVerifiedProperty"))
                    ));
                }
            }
            if (criteria.verifiedRealtor() != null) {
                if (criteria.verifiedRealtor()) {
                    predicates.add(cb.isTrue(root.get("isVerifiedRealtor").as(Boolean.class)));
                } else {
                    predicates.add(cb.or(
                            cb.isFalse(root.get("isVerifiedRealtor").as(Boolean.class)),
                            cb.isNull(root.get("isVerifiedRealtor"))
                    ));
                }
            }
            if (Boolean.TRUE.equals(criteria.hideDuplicates())) {
                predicates.add(cb.or(
                        cb.isFalse(root.get("isDuplicate").as(Boolean.class)),
                        cb.isNull(root.get("isDuplicate"))
                ));
            }
            if (criteria.petsAllowed() != null) {
                predicates.add(cb.equal(root.get("rules").get("petsAllowed"), criteria.petsAllowed()));
            }
            if (criteria.amenityIds() != null && !criteria.amenityIds().isEmpty()) {
                Join<Property, Amenity> amenitiesByIdJoin = root.join("amenities", JoinType.INNER);
                predicates.add(amenitiesByIdJoin.get("id").in(criteria.amenityIds()));
                query.distinct(true);
            }
            if (criteria.amenitySlugs() != null && !criteria.amenitySlugs().isEmpty()) {
                List<String> normalizedSlugs = criteria.amenitySlugs().stream()
                        .filter(slug -> slug != null && !slug.isBlank())
                        .map(slug -> slug.toLowerCase(Locale.ROOT))
                        .toList();
                if (!normalizedSlugs.isEmpty()) {
                    Join<Property, Amenity> amenitiesBySlugJoin = root.join("amenities", JoinType.INNER);
                    predicates.add(cb.lower(amenitiesBySlugJoin.get("slug")).in(normalizedSlugs));
                    query.distinct(true);
                }
            }
            if (criteria.amenityCategories() != null && !criteria.amenityCategories().isEmpty()) {
                Join<Property, Amenity> amenitiesByCategoryJoin = root.join("amenities", JoinType.INNER);
                predicates.add(amenitiesByCategoryJoin.get("category").in(criteria.amenityCategories()));
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
