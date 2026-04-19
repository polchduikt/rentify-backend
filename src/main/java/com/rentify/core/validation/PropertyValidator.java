package com.rentify.core.validation;

import com.rentify.core.dto.property.AddressDto;
import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PropertyValidator extends AbstractValidator {

    public PropertyValidator(Validator validator) {
        super(validator);
    }

    public void validateCreateOrUpdateRequest(PropertyCreateRequestDto request) {
        Map<String, String> errors = collectBeanErrors(request);
        if (request.floor() != null && request.totalFloors() != null && request.floor() > request.totalFloors()) {
            errors.put("floor", "must be less than or equal to totalFloors");
        }
        if (request.areaSqm() != null && request.areaSqm().compareTo(BigDecimal.ZERO) <= 0) {
            errors.put("areaSqm", "must be greater than 0");
        }
        if ((request.checkInTime() == null) != (request.checkOutTime() == null)) {
            errors.put("checkInTime", "both values must be provided together");
            errors.put("checkOutTime", "both values must be provided together");
        }
        if (request.checkInTime() != null && request.checkOutTime() != null
                && !request.checkInTime().isBefore(request.checkOutTime())) {
            errors.put("checkInTime", "must be before checkOutTime");
        }
        validateAddressReferences(request.address(), errors);
        throwIfAny(errors);
    }

    public void validateSearchCriteria(PropertySearchCriteriaDto criteria) {
        Map<String, String> errors = new LinkedHashMap<>();
        validateIdFields(criteria, errors);
        validateMinMaxRanges(criteria, errors);
        validateDateRange(criteria, errors);
        validateGeoLocation(criteria, errors);
        boolean hasFullBounds = validateMapBounds(criteria, errors);
        validateMapBoundsConflicts(criteria, hasFullBounds, errors);
        throwIfAny(errors);
    }

    private void validateIdFields(PropertySearchCriteriaDto criteria, Map<String, String> errors) {
        validatePositiveId("propertyId", criteria.propertyId(), errors);
        validatePositiveId("cityId", criteria.cityId(), errors);
        validatePositiveId("districtId", criteria.districtId(), errors);
        validatePositiveId("metroStationId", criteria.metroStationId(), errors);
        validatePositiveId("residentialComplexId", criteria.residentialComplexId(), errors);
    }

    private void validateMinMaxRanges(PropertySearchCriteriaDto criteria, Map<String, String> errors) {
        validateMinMax("minPrice", criteria.minPrice(), "maxPrice", criteria.maxPrice(), errors);
        validateMinMax("minRooms", criteria.minRooms(), "maxRooms", criteria.maxRooms(), errors);
        validateMinMax("minFloor", criteria.minFloor(), "maxFloor", criteria.maxFloor(), errors);
        validateMinMax("minTotalFloors", criteria.minTotalFloors(), "maxTotalFloors", criteria.maxTotalFloors(), errors);
        validateMinMax("minSleepingPlaces", criteria.minSleepingPlaces(), "maxSleepingPlaces", criteria.maxSleepingPlaces(), errors);
        validateMinMax("minArea", criteria.minArea(), "maxArea", criteria.maxArea(), errors);
    }

    private void validateDateRange(PropertySearchCriteriaDto criteria, Map<String, String> errors) {
        if ((criteria.dateFrom() == null) != (criteria.dateTo() == null)) {
            errors.put("dateFrom", "both values must be provided together");
            errors.put("dateTo", "both values must be provided together");
        }
        if (criteria.dateFrom() != null && criteria.dateTo() != null
                && !criteria.dateFrom().isBefore(criteria.dateTo())) {
            errors.put("dateFrom", "must be before dateTo");
        }
    }

    private void validateGeoLocation(PropertySearchCriteriaDto criteria, Map<String, String> errors) {
        if ((criteria.lat() == null) != (criteria.lng() == null)) {
            errors.put("lat", "both values must be provided together");
            errors.put("lng", "both values must be provided together");
        }
        if (criteria.lat() != null && (criteria.lat() < -90 || criteria.lat() > 90)) {
            errors.put("lat", "must be between -90 and 90");
        }
        if (criteria.lng() != null && (criteria.lng() < -180 || criteria.lng() > 180)) {
            errors.put("lng", "must be between -180 and 180");
        }
        if (criteria.radiusKm() != null && criteria.radiusKm() <= 0) {
            errors.put("radiusKm", "must be greater than 0");
        }
        if (criteria.radiusKm() != null && criteria.lat() == null) {
            errors.put("radiusKm", "requires lat/lng to be provided");
        }
    }

    private boolean validateMapBounds(PropertySearchCriteriaDto criteria, Map<String, String> errors) {
        boolean hasAnyBoundsValue = criteria.southWestLat() != null
                || criteria.southWestLng() != null
                || criteria.northEastLat() != null
                || criteria.northEastLng() != null;
        boolean hasFullBounds = criteria.southWestLat() != null
                && criteria.southWestLng() != null
                && criteria.northEastLat() != null
                && criteria.northEastLng() != null;
        if (hasAnyBoundsValue && !hasFullBounds) {
            errors.put("mapBounds", "southWestLat/southWestLng/northEastLat/northEastLng: all values must be provided together");
        }
        if (criteria.southWestLat() != null && (criteria.southWestLat() < -90 || criteria.southWestLat() > 90)) {
            errors.put("southWestLat", "must be between -90 and 90");
        }
        if (criteria.northEastLat() != null && (criteria.northEastLat() < -90 || criteria.northEastLat() > 90)) {
            errors.put("northEastLat", "must be between -90 and 90");
        }
        if (criteria.southWestLng() != null && (criteria.southWestLng() < -180 || criteria.southWestLng() > 180)) {
            errors.put("southWestLng", "must be between -180 and 180");
        }
        if (criteria.northEastLng() != null && (criteria.northEastLng() < -180 || criteria.northEastLng() > 180)) {
            errors.put("northEastLng", "must be between -180 and 180");
        }
        if (hasFullBounds && criteria.southWestLat() > criteria.northEastLat()) {
            errors.put("southWestLat", "must be less than or equal to northEastLat");
        }
        return hasFullBounds;
    }

    private void validateMapBoundsConflicts(
            PropertySearchCriteriaDto criteria,
            boolean hasFullBounds,
            Map<String, String> errors
    ) {
        if (hasFullBounds && criteria.radiusKm() != null) {
            errors.put("radiusKm", "cannot be used together with map bounds");
        }
        if (hasFullBounds && (criteria.lat() != null || criteria.lng() != null)) {
            errors.put("latLng", "cannot be used together with map bounds");
        }
    }

    private void validateAddressReferences(AddressDto address, Map<String, String> errors) {
        if (address == null) {
            return;
        }
        if (address.cityId() != null && address.cityId() <= 0) {
            errors.put("address.cityId", "must be greater than 0");
        }
        if (address.districtId() != null && address.districtId() <= 0) {
            errors.put("address.districtId", "must be greater than 0");
        }
        if (address.metroStationId() != null && address.metroStationId() <= 0) {
            errors.put("address.metroStationId", "must be greater than 0");
        }
        if (address.residentialComplexId() != null && address.residentialComplexId() <= 0) {
            errors.put("address.residentialComplexId", "must be greater than 0");
        }
    }

    private void validatePositiveId(String fieldName, Long value, Map<String, String> errors) {
        if (value != null && value <= 0) {
            errors.put(fieldName, "must be greater than 0");
        }
    }

    private <T extends Comparable<T>> void validateMinMax(
            String minFieldName,
            T minValue,
            String maxFieldName,
            T maxValue,
            Map<String, String> errors
    ) {
        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            errors.put(minFieldName, "must be less than or equal to " + maxFieldName);
        }
    }
}
