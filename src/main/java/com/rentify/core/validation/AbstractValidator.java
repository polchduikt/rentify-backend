package com.rentify.core.validation;

import com.rentify.core.exception.ApiValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractValidator {

    private final Validator validator;

    protected AbstractValidator(Validator validator) {
        this.validator = validator;
    }

    protected <T> Map<String, String> collectBeanErrors(T target) {
        Set<ConstraintViolation<T>> violations = validator.validate(target);
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<T> violation : violations) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return errors;
    }

    protected void throwIfAny(Map<String, String> errors) {
        if (!errors.isEmpty()) {
            throw new ApiValidationException(errors);
        }
    }
}
