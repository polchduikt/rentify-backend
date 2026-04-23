package com.rentify.core.validation;

import com.rentify.core.exception.DomainException;

import java.time.LocalDate;
import java.util.Map;

public final class DateRangeUtils {

    private DateRangeUtils() {
    }

    public static void requireBothOrNone(
            LocalDate from, LocalDate to,
            String fromField, String toField,
            Map<String, String> errors
    ) {
        if ((from == null) != (to == null)) {
            errors.put(fromField, "both values must be provided together");
            errors.put(toField, "both values must be provided together");
        }
    }

    public static void requireFromBeforeTo(
            LocalDate from, LocalDate to,
            String fromField,
            Map<String, String> errors
    ) {
        if (from != null && to != null && !from.isBefore(to)) {
            errors.put(fromField, "must be before " + fromField.replace("dateFrom", "dateTo").replace("From", "To"));
        }
    }

    public static void assertFromBeforeOrEqualTo(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw DomainException.badRequest("DATE_RANGE_INVALID", "dateFrom must be before or equal to dateTo");
        }
    }

    public static void assertBothOrNone(LocalDate from, LocalDate to) {
        if ((from == null) != (to == null)) {
            throw DomainException.badRequest("DATE_RANGE_INVALID", "Both dateFrom and dateTo must be provided together.");
        }
    }

    public static void assertFromStrictlyBeforeTo(LocalDate from, LocalDate to) {
        if (from != null && to != null && !from.isBefore(to)) {
            throw DomainException.badRequest("DATE_RANGE_INVALID", "dateFrom must be before dateTo.");
        }
    }
}
