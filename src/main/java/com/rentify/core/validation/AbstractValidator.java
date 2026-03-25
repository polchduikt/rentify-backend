package com.rentify.core.validation;

import com.rentify.core.exception.ApiValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractValidator {

    private final Validator validator;

    protected AbstractValidator(Validator validator) {
        this.validator = validator;
    }

    protected <T> Set<String> collectBeanErrors(T target) {
        Set<ConstraintViolation<T>> violations = validator.validate(target);
        Set<String> errors = new LinkedHashSet<>();
        for (ConstraintViolation<T> violation : violations) {
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }
        return errors;
    }

    protected void throwIfAny(Set<String> errors) {
        if (!errors.isEmpty()) {
            throw new ApiValidationException(errors);
        }
    }
}
