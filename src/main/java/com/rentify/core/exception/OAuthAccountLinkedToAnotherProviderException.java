package com.rentify.core.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class OAuthAccountLinkedToAnotherProviderException extends DomainException {
    public OAuthAccountLinkedToAnotherProviderException(String message) {
        super(HttpStatus.CONFLICT, "OAUTH_PROVIDER_CONFLICT", message, Map.of());
    }
}
