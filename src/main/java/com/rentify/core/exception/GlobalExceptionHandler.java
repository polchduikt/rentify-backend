package com.rentify.core.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;
import java.net.ConnectException;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidGoogleTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidGoogleTokenException(
            InvalidGoogleTokenException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, "INVALID_GOOGLE_TOKEN", Map.of(), ex);
    }

    @ExceptionHandler(OAuthAccountLinkedToAnotherProviderException.class)
    public ResponseEntity<ApiErrorResponse> handleOAuthAccountLinkedToAnotherProviderException(
            OAuthAccountLinkedToAnotherProviderException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, "OAUTH_PROVIDER_CONFLICT", Map.of(), ex);
    }

    @ExceptionHandler(ApiValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleApiValidationException(
            ApiValidationException ex, HttpServletRequest request) {
        return build(
                HttpStatus.BAD_REQUEST,
                String.join(", ", ex.getErrors()),
                request,
                "API_VALIDATION_ERROR",
                Map.of(),
                ex
        );
    }

    @ExceptionHandler(AccountDeactivatedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountDeactivatedException(
            AccountDeactivatedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request, "ACCOUNT_DEACTIVATED", Map.of(), ex);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, "ENTITY_NOT_FOUND", Map.of(), ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, "INVALID_ARGUMENT", Map.of(), ex);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", request, "BAD_CREDENTIALS", Map.of(), ex);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleDisabledException(
            DisabledException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Account is deactivated", request, "ACCOUNT_DISABLED", Map.of(), ex);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, "ILLEGAL_STATE", Map.of(), ex);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request, "ACCESS_DENIED", Map.of(), ex);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        String message = ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Authentication failed"
                : ex.getMessage();
        return build(HttpStatus.UNAUTHORIZED, message, request, "AUTHENTICATION_FAILED", Map.of(), ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Data integrity violation", request, "DATA_INTEGRITY_VIOLATION", Map.of(), ex);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiErrorResponse> handleFileUploadException(
            FileUploadException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request, "FILE_UPLOAD_ERROR", Map.of(), ex);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "Uploaded file exceeds maximum allowed size", request, "MAX_UPLOAD_SIZE_EXCEEDED", Map.of(), ex);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestPartException(
            MissingServletRequestPartException ex, HttpServletRequest request) {
        String message = "Missing required request part: " + ex.getRequestPartName();
        return build(HttpStatus.BAD_REQUEST, message, request, "MISSING_REQUEST_PART", Map.of(), ex);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiErrorResponse> handleMultipartException(
            MultipartException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid multipart request", request, "MULTIPART_ERROR", Map.of(), ex);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        String message = ex.getContentType() == null
                ? "Unsupported Content-Type"
                : "Unsupported Content-Type: " + ex.getContentType();
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message, request, "UNSUPPORTED_MEDIA_TYPE", Map.of(), ex);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMediaTypeNotAcceptableException(
            HttpMediaTypeNotAcceptableException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_ACCEPTABLE, "Requested response media type is not acceptable", request, "NOT_ACCEPTABLE", Map.of(), ex);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason();
        if (message == null || message.isBlank()) {
            message = status.getReasonPhrase();
        }
        return build(status, message, request, "RESPONSE_STATUS", Map.of(), ex);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Resource not found", request, "RESOURCE_NOT_FOUND", Map.of(), ex);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Endpoint not found", request, "ENDPOINT_NOT_FOUND", Map.of(), ex);
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<ApiErrorResponse> handleAsyncRequestTimeoutException(
            AsyncRequestTimeoutException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Request timed out", request, "ASYNC_REQUEST_TIMEOUT", Map.of(), ex);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLockingFailureException(
            OptimisticLockingFailureException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Concurrent update conflict", request, "OPTIMISTIC_LOCK_CONFLICT", Map.of(), ex);
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handlePessimisticLockingFailureException(
            PessimisticLockingFailureException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "Database lock conflict", request, "PESSIMISTIC_LOCK_CONFLICT", Map.of(), ex);
    }

    @ExceptionHandler(CannotAcquireLockException.class)
    public ResponseEntity<ApiErrorResponse> handleCannotAcquireLockException(
            CannotAcquireLockException ex, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Database is busy. Please retry.", request, "DB_LOCK_UNAVAILABLE", Map.of(), ex);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAccessException(
            ResourceAccessException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "Upstream service is unavailable", request, "UPSTREAM_UNAVAILABLE", Map.of(), ex);
    }

    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<ApiErrorResponse> handleConnectException(
            ConnectException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "Failed to connect to upstream service", request, "UPSTREAM_CONNECT_ERROR", Map.of(), ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request", request, "MALFORMED_JSON", Map.of(), ex);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("Invalid value '%s' for parameter '%s'",
                ex.getValue(), ex.getName());
        return build(HttpStatus.BAD_REQUEST, message, request, "ARGUMENT_TYPE_MISMATCH", Map.of(), ex);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = "Missing required parameter: " + ex.getParameterName();
        return build(HttpStatus.BAD_REQUEST, message, request, "MISSING_REQUEST_PARAMETER", Map.of(), ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        return build(
                HttpStatus.BAD_REQUEST,
                formatFieldErrors(fieldErrors),
                request,
                "VALIDATION_ERROR",
                fieldErrors,
                ex
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(
            BindException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .filter(error -> error.getField() != null)
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> Objects.toString(error.getDefaultMessage(), "Invalid value"),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        return build(
                HttpStatus.BAD_REQUEST,
                formatFieldErrors(fieldErrors),
                request,
                "VALIDATION_ERROR",
                fieldErrors,
                ex
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String message = "HTTP method not allowed: " + ex.getMethod();
        return build(HttpStatus.METHOD_NOT_ALLOWED, message, request, "METHOD_NOT_ALLOWED", Map.of(), ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .filter(error -> error.getField() != null)
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> Objects.toString(error.getDefaultMessage(), "Invalid value"),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        return build(
                HttpStatus.BAD_REQUEST,
                formatFieldErrors(fieldErrors),
                request,
                "VALIDATION_ERROR",
                fieldErrors,
                ex
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(
            Exception ex, HttpServletRequest request) {
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                request,
                "UNEXPECTED_ERROR",
                Map.of(),
                ex
        );
    }

    private String formatFieldErrors(Map<String, String> fieldErrors) {
        return fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            String errorCode,
            Map<String, String> fieldErrors,
            Exception ex
    ) {
        logException(status, request, errorCode, ex);
        ApiErrorResponse errorResponse = new ApiErrorResponse(
                ZonedDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                errorCode,
                fieldErrors == null ? Map.of() : fieldErrors
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    private void logException(HttpStatus status, HttpServletRequest request, String errorCode, Exception ex) {
        if (ex == null) {
            return;
        }

        String method = request.getMethod();
        String path = request.getRequestURI();

        if (status.is5xxServerError()) {
            logger.error("API error {} for {} {}: {}", errorCode, method, path, ex.getMessage(), ex);
            return;
        }
        logger.warn("API error {} for {} {}: {}", errorCode, method, path, ex.getMessage());
    }
}
