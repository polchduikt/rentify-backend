package com.rentify.core.repository.specification;

import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import com.rentify.core.entity.AvailabilityBlock;
import com.rentify.core.entity.Booking;
import com.rentify.core.entity.Property;
import com.rentify.core.enums.BookingStatus;
import com.rentify.core.enums.RentalType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PropertySpecifications {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double KM_PER_LAT_DEGREE = 111.32;

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
                predicates.add(cb.equal(root.get("address").get("cityRef").get("country"), criteria.country()));
            }
            if (criteria.region() != null && !criteria.region().isBlank()) {
                predicates.add(cb.equal(root.get("address").get("cityRef").get("region"), criteria.region()));
            }
            if (criteria.city() != null && !criteria.city().isBlank()) {
                predicates.add(cb.equal(root.get("address").get("cityRef").get("name"), criteria.city()));
            }
            if (criteria.lat() != null && criteria.lng() != null && criteria.radiusKm() != null) {
                predicates.add(cb.isNotNull(root.get("address").get("lat")));
                predicates.add(cb.isNotNull(root.get("address").get("lng")));
                double latDelta = criteria.radiusKm() / KM_PER_LAT_DEGREE;
                double cosLat = Math.cos(Math.toRadians(criteria.lat()));
                double lngDelta = Math.abs(cosLat) < 1e-6
                        ? 180.0
                        : Math.min(180.0, criteria.radiusKm() / (KM_PER_LAT_DEGREE * Math.abs(cosLat)));
                double minLat = Math.max(-90.0, criteria.lat() - latDelta);
                double maxLat = Math.min(90.0, criteria.lat() + latDelta);
                double minLng = criteria.lng() - lngDelta;
                double maxLng = criteria.lng() + lngDelta;
                predicates.add(cb.between(
                        root.get("address").get("lat").as(Double.class),
                        minLat,
                        maxLat
                ));
                if (minLng >= -180.0 && maxLng <= 180.0) {
                    predicates.add(cb.between(
                            root.get("address").get("lng").as(Double.class),
                            minLng,
                            maxLng
                    ));
                } else {
                    double normalizedMinLng = minLng < -180.0 ? minLng + 360.0 : minLng;
                    double normalizedMaxLng = maxLng > 180.0 ? maxLng - 360.0 : maxLng;
                    predicates.add(cb.or(
                            cb.greaterThanOrEqualTo(root.get("address").get("lng").as(Double.class), normalizedMinLng),
                            cb.lessThanOrEqualTo(root.get("address").get("lng").as(Double.class), normalizedMaxLng)
                    ));
                }
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
                        cb.literal(EARTH_RADIUS_KM),
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
            Expression<BigDecimal> comparablePrice = resolveComparablePriceExpression(criteria, root, cb);
            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(comparablePrice, criteria.minPrice()));
            }
            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(comparablePrice, criteria.maxPrice()));
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
                        cb.not(bookingRoot.get("status").in(
                                List.of(BookingStatus.CANCELLED, BookingStatus.REJECTED, BookingStatus.COMPLETED)
                        )),
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
            if (criteria.propertyType() != null) {
                predicates.add(cb.equal(
                        cb.lower(root.get("propertyType").as(String.class)),
                        criteria.propertyType().dbValue()
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
                var amenityByIdSubquery = query.subquery(Long.class);
                var amenityByIdRoot = amenityByIdSubquery.from(Property.class);
                var amenityByIdJoin = amenityByIdRoot.join("amenities");
                amenityByIdSubquery.select(cb.literal(1L));
                amenityByIdSubquery.where(
                        cb.equal(amenityByIdRoot.get("id"), root.get("id")),
                        amenityByIdJoin.get("id").in(criteria.amenityIds())
                );
                predicates.add(cb.exists(amenityByIdSubquery));
            }
            if (criteria.amenitySlugs() != null && !criteria.amenitySlugs().isEmpty()) {
                List<String> normalizedSlugs = criteria.amenitySlugs().stream()
                        .filter(slug -> slug != null && !slug.isBlank())
                        .map(slug -> slug.toLowerCase(Locale.ROOT))
                        .toList();
                if (!normalizedSlugs.isEmpty()) {
                    var amenityBySlugSubquery = query.subquery(Long.class);
                    var amenityBySlugRoot = amenityBySlugSubquery.from(Property.class);
                    var amenityBySlugJoin = amenityBySlugRoot.join("amenities");
                    amenityBySlugSubquery.select(cb.literal(1L));
                    amenityBySlugSubquery.where(
                            cb.equal(amenityBySlugRoot.get("id"), root.get("id")),
                            cb.lower(amenityBySlugJoin.get("slug")).in(normalizedSlugs)
                    );
                    predicates.add(cb.exists(amenityBySlugSubquery));
                }
            }
            if (criteria.amenityCategories() != null && !criteria.amenityCategories().isEmpty()) {
                var amenityByCategorySubquery = query.subquery(Long.class);
                var amenityByCategoryRoot = amenityByCategorySubquery.from(Property.class);
                var amenityByCategoryJoin = amenityByCategoryRoot.join("amenities");
                amenityByCategorySubquery.select(cb.literal(1L));
                amenityByCategorySubquery.where(
                        cb.equal(amenityByCategoryRoot.get("id"), root.get("id")),
                        amenityByCategoryJoin.get("category").in(criteria.amenityCategories())
                );
                predicates.add(cb.exists(amenityByCategorySubquery));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Expression<BigDecimal> resolveComparablePriceExpression(
            PropertySearchCriteriaDto criteria,
            Root<Property> root,
            CriteriaBuilder cb
    ) {
        Expression<BigDecimal> pricePerNight = root.get("pricing").get("pricePerNight");
        Expression<BigDecimal> pricePerMonth = root.get("pricing").get("pricePerMonth");

        Expression<BigDecimal> shortTermPrice = cb.<BigDecimal>coalesce()
                .value(pricePerNight)
                .value(pricePerMonth);
        Expression<BigDecimal> longTermPrice = cb.<BigDecimal>coalesce()
                .value(pricePerMonth)
                .value(pricePerNight);

        if (criteria.rentalType() == RentalType.SHORT_TERM) {
            return shortTermPrice;
        }
        if (criteria.rentalType() == RentalType.LONG_TERM) {
            return longTermPrice;
        }

        return cb.<BigDecimal>selectCase()
                .when(cb.equal(root.get("rentalType"), RentalType.LONG_TERM), longTermPrice)
                .otherwise(shortTermPrice);
    }
}
