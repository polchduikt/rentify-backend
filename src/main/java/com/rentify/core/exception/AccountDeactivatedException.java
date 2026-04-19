package com.rentify.core.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class AccountDeactivatedException extends DomainException {
    public AccountDeactivatedException(String message) {
        super(HttpStatus.FORBIDDEN, "ACCOUNT_DEACTIVATED", message, Map.of());
    }
}
