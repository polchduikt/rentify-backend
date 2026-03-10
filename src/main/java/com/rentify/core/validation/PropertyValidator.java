package com.rentify.core.validation;

import com.rentify.core.dto.property.AddressDto;
import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import com.rentify.core.exception.ApiValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PropertyValidator {

    private final Validator validator;

    public void validateCreateOrUpdateRequest(PropertyCreateRequestDto request) {
        Set<String> errors = collectBeanErrors(request);
        if (request.floor() != null && request.totalFloors() != null && request.floor() > request.totalFloors()) {
            errors.add("floor: must be less than or equal to totalFloors");
        }
        if (request.areaSqm() != null && request.areaSqm().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("areaSqm: must be greater than 0");
        }
        if ((request.checkInTime() == null) != (request.checkOutTime() == null)) {
            errors.add("checkInTime/checkOutTime: both values must be provided together");
        }
        if (request.checkInTime() != null && request.checkOutTime() != null
                && !request.checkInTime().isBefore(request.checkOutTime())) {
            errors.add("checkInTime: must be before checkOutTime");
        }
        validateAddressReferences(request.address(), errors);
        throwIfAny(errors);
    }

    public void validateSearchCriteria(PropertySearchCriteriaDto criteria) {
        Set<String> errors = new LinkedHashSet<>();
        validatePositiveId("propertyId", criteria.propertyId(), errors);
        validatePositiveId("cityId", criteria.cityId(), errors);
        validatePositiveId("districtId", criteria.districtId(), errors);
        validatePositiveId("metroStationId", criteria.metroStationId(), errors);
        validatePositiveId("residentialComplexId", criteria.residentialComplexId(), errors);
        validateMinMax("minPrice", criteria.minPrice(), "maxPrice", criteria.maxPrice(), errors);
        validateMinMax("minRooms", criteria.minRooms(), "maxRooms", criteria.maxRooms(), errors);
        validateMinMax("minFloor", criteria.minFloor(), "maxFloor", criteria.maxFloor(), errors);
        validateMinMax("minTotalFloors", criteria.minTotalFloors(), "maxTotalFloors", criteria.maxTotalFloors(), errors);
        validateMinMax("minSleepingPlaces", criteria.minSleepingPlaces(), "maxSleepingPlaces", criteria.maxSleepingPlaces(), errors);
        validateMinMax("minArea", criteria.minArea(), "maxArea", criteria.maxArea(), errors);

        if ((criteria.dateFrom() == null) != (criteria.dateTo() == null)) {
            errors.add("dateFrom/dateTo: both values must be provided together");
        }
        if (criteria.dateFrom() != null && criteria.dateTo() != null
                && !criteria.dateFrom().isBefore(criteria.dateTo())) {
            errors.add("dateFrom: must be before dateTo");
        }

        if ((criteria.lat() == null) != (criteria.lng() == null)) {
            errors.add("lat/lng: both values must be provided together");
        }
        if (criteria.lat() != null && (criteria.lat() < -90 || criteria.lat() > 90)) {
            errors.add("lat: must be between -90 and 90");
        }
        if (criteria.lng() != null && (criteria.lng() < -180 || criteria.lng() > 180)) {
            errors.add("lng: must be between -180 and 180");
        }
        if (criteria.radiusKm() != null && criteria.radiusKm() <= 0) {
            errors.add("radiusKm: must be greater than 0");
        }
        if (criteria.radiusKm() != null && criteria.lat() == null) {
            errors.add("radiusKm: requires lat/lng to be provided");
        }

        throwIfAny(errors);
    }

    private void validateAddressReferences(AddressDto address, Set<String> errors) {
        if (address == null) {
            return;
        }
        if (address.cityId() != null && address.cityId() <= 0) {
            errors.add("address.cityId: must be greater than 0");
        }
        if (address.districtId() != null && address.districtId() <= 0) {
            errors.add("address.districtId: must be greater than 0");
        }
        if (address.metroStationId() != null && address.metroStationId() <= 0) {
            errors.add("address.metroStationId: must be greater than 0");
        }
        if (address.residentialComplexId() != null && address.residentialComplexId() <= 0) {
            errors.add("address.residentialComplexId: must be greater than 0");
        }
    }

    private void validatePositiveId(String fieldName, Long value, Set<String> errors) {
        if (value != null && value <= 0) {
            errors.add(fieldName + ": must be greater than 0");
        }
    }

    private <T extends Comparable<T>> void validateMinMax(
            String minFieldName,
            T minValue,
            String maxFieldName,
            T maxValue,
            Set<String> errors
    ) {
        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            errors.add(minFieldName + ": must be less than or equal to " + maxFieldName);
        }
    }

    private <T> Set<String> collectBeanErrors(T target) {
        Set<ConstraintViolation<T>> violations = validator.validate(target);
        Set<String> errors = new LinkedHashSet<>();
        for (ConstraintViolation<T> violation : violations) {
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }
        return errors;
    }

    private void throwIfAny(Set<String> errors) {
        if (!errors.isEmpty()) {
            throw new ApiValidationException(errors);
        }
    }
}
