package com.rentify.core.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ApiValidationException extends DomainException {

    public ApiValidationException(Map<String, String> fieldErrors) {
        super(HttpStatus.BAD_REQUEST, "API_VALIDATION_ERROR", "Validation failed", fieldErrors);
    }

}
