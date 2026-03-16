package com.rentify.core.exception;

import java.time.ZonedDateTime;

public record ApiErrorResponse(
        ZonedDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}
