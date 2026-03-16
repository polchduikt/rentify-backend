package com.rentify.core.validation;

import com.rentify.core.dto.booking.BookingRequestDto;
import com.rentify.core.exception.ApiValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BookingValidator {

    private final Validator validator;

    public void validateCreateBookingRequest(BookingRequestDto request) {
        Set<String> errors = collectBeanErrors(request);

        if (request.propertyId() != null && request.propertyId() <= 0) {
            errors.add("propertyId: must be greater than 0");
        }
        if (request.dateFrom() != null && request.dateTo() != null && !request.dateFrom().isBefore(request.dateTo())) {
            errors.add("dateFrom: must be before dateTo");
        }

        throwIfAny(errors);
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
