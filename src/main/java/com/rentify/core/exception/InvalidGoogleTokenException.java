package com.rentify.core.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class InvalidGoogleTokenException extends DomainException {
    public InvalidGoogleTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, "INVALID_GOOGLE_TOKEN", message, Map.of());
    }
}
