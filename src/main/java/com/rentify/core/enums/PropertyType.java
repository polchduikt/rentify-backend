package com.rentify.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Locale;

public enum PropertyType {
    APARTMENT("apartment"),
    HOUSE("house"),
    ROOM("room"),
    STUDIO("studio"),
    LOFT("loft"),
    PENTHOUSE("penthouse"),
    TOWNHOUSE("townhouse"),
    VILLA("villa"),
    OTHER("other");

    private final String value;

    PropertyType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static PropertyType fromValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(type -> type.value.equals(normalized) || type.name().toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported propertyType: " + rawValue));
    }
}
