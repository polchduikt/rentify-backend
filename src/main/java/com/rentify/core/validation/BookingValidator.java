package com.rentify.core.validation;

import com.rentify.core.dto.booking.BookingRequestDto;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.RentalType;
import com.rentify.core.exception.DomainException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.Map;

@Component
public class BookingValidator extends AbstractValidator {

    public BookingValidator(Validator validator) {
        super(validator);
    }

    public void validateCreateBookingRequest(BookingRequestDto request) {
        Map<String, String> errors = collectBeanErrors(request);

        if (request.propertyId() != null && request.propertyId() <= 0) {
            errors.put("propertyId", "must be greater than 0");
        }
        if (request.dateFrom() != null && request.dateTo() != null && !request.dateFrom().isBefore(request.dateTo())) {
            errors.put("dateFrom", "must be before dateTo");
        }

        throwIfAny(errors);
    }

    public void validateBookingEligibility(Property property, User tenant, BookingRequestDto request) {
        if (property.getStatus() != PropertyStatus.ACTIVE) {
            throw DomainException.conflict("BOOKING_PROPERTY_NOT_ACTIVE", "Only active properties can be booked.");
        }
        if (property.getRentalType() != RentalType.SHORT_TERM) {
            throw DomainException.conflict("BOOKING_RENTAL_TYPE_NOT_ALLOWED", "Only short-term properties can be booked.");
        }
        if (property.getHost().getId().equals(tenant.getId())) {
            throw DomainException.badRequest("BOOKING_SELF_NOT_ALLOWED", "You cannot book your own property.");
        }

        long nights = ChronoUnit.DAYS.between(request.dateFrom(), request.dateTo());
        if (nights <= 0) {
            throw DomainException.badRequest("BOOKING_DATES_INVALID", "Check-out date must be after check-in date.");
        }
        if (property.getMaxGuests() == null) {
            throw DomainException.internal("PROPERTY_CONFIGURATION_INVALID", "Property configuration is invalid: maxGuests is not set.");
        }
        if (request.guests() > property.getMaxGuests()) {
            throw DomainException.badRequest(
                    "BOOKING_GUESTS_EXCEED_CAPACITY",
                    "Guest count exceeds the maximum capacity of " + property.getMaxGuests() + " for this property.",
                    java.util.Map.of("maxGuests", String.valueOf(property.getMaxGuests()))
            );
        }
    }

    public void validateAvailability(boolean hasBlockedDates, boolean isOccupied) {
        if (hasBlockedDates) {
            throw DomainException.conflict("BOOKING_DATES_BLOCKED", "The property is blocked by the host for the selected dates.");
        }
        if (isOccupied) {
            throw DomainException.conflict("BOOKING_DATES_OCCUPIED", "The property is already booked for the selected dates.");
        }
    }
}
