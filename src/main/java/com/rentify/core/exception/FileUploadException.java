package com.rentify.core.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class FileUploadException extends DomainException {

    public FileUploadException(String message) {
        super(HttpStatus.BAD_GATEWAY, "FILE_UPLOAD_ERROR", message, Map.of());
    }

    public FileUploadException(String message, Throwable cause) {
        super(HttpStatus.BAD_GATEWAY, "FILE_UPLOAD_ERROR", message, Map.of());
        this.initCause(cause);
    }
}
