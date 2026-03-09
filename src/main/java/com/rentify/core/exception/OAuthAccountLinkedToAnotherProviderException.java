package com.rentify.core.exception;

public class OAuthAccountLinkedToAnotherProviderException extends RuntimeException {
    public OAuthAccountLinkedToAnotherProviderException(String message) {
        super(message);
    }
}
