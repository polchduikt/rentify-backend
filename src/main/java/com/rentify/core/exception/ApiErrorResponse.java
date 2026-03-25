package com.rentify.core.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.ZonedDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        ZonedDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        String errorCode,
        Map<String, String> fieldErrors
) {}
