package com.rentify.core.exception;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ApiValidationException extends RuntimeException {

    private final Set<String> errors;

    public ApiValidationException(Set<String> errors) {
        super(errors.stream().collect(Collectors.joining(", ")));
        this.errors = Collections.unmodifiableSet(new LinkedHashSet<>(errors));
    }

    public Set<String> getErrors() {
        return errors;
    }
}
