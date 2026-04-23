package com.rentify.core.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DomainException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Map<String, String> details;

    public DomainException(HttpStatus status, String errorCode, String message, Map<String, String> details) {
        super(message);
        this.status = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
        this.errorCode = errorCode == null || errorCode.isBlank() ? "DOMAIN_ERROR" : errorCode;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public static DomainException badRequest(String errorCode, String message) {
        return new DomainException(HttpStatus.BAD_REQUEST, errorCode, message, Map.of());
    }

    public static DomainException badRequest(String errorCode, String message, Map<String, String> details) {
        return new DomainException(HttpStatus.BAD_REQUEST, errorCode, message, details);
    }

    public static DomainException conflict(String errorCode, String message) {
        return new DomainException(HttpStatus.CONFLICT, errorCode, message, Map.of());
    }

    public static DomainException conflict(String errorCode, String message, Map<String, String> details) {
        return new DomainException(HttpStatus.CONFLICT, errorCode, message, details);
    }

    public static DomainException serviceUnavailable(String errorCode, String message) {
        return new DomainException(HttpStatus.SERVICE_UNAVAILABLE, errorCode, message, Map.of());
    }

    public static DomainException internal(String errorCode, String message) {
        return new DomainException(HttpStatus.INTERNAL_SERVER_ERROR, errorCode, message, Map.of());
    }
}

