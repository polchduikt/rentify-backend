package com.rentify.core.validation;

import com.rentify.core.dto.booking.BookingRequestDto;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.RentalType;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.Set;

@Component
public class BookingValidator extends AbstractValidator {

    public BookingValidator(Validator validator) {
        super(validator);
    }

    public void validateCreateBookingRequest(BookingRequestDto request) {
        Set<String> errors = collectBeanErrors(request);

        if (request.propertyId() != null && request.propertyId() <= 0) {
            errors.add("propertyId: must be greater than 0");
        }
        if (request.dateFrom() != null && request.dateTo() != null && !request.dateFrom().isBefore(request.dateTo())) {
            errors.add("dateFrom: must be before dateTo");
        }

        throwIfAny(errors);
    }

    public void validateBookingEligibility(Property property, User tenant, BookingRequestDto request) {
        if (property.getStatus() != PropertyStatus.ACTIVE) {
            throw new IllegalStateException("Only active properties can be booked.");
        }
        if (property.getRentalType() != RentalType.SHORT_TERM) {
            throw new IllegalStateException("Only short-term properties can be booked.");
        }
        if (property.getHost().getId().equals(tenant.getId())) {
            throw new IllegalArgumentException("You cannot book your own property.");
        }

        long nights = ChronoUnit.DAYS.between(request.dateFrom(), request.dateTo());
        if (nights <= 0) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
        if (property.getMaxGuests() == null) {
            throw new IllegalStateException("Property configuration is invalid: maxGuests is not set.");
        }
        if (request.guests() > property.getMaxGuests()) {
            throw new IllegalArgumentException(
                    "Guest count exceeds the maximum capacity of " + property.getMaxGuests() + " for this property.");
        }
    }

    public void validateAvailability(boolean hasBlockedDates, boolean isOccupied) {
        if (hasBlockedDates) {
            throw new IllegalStateException("The property is blocked by the host for the selected dates.");
        }
        if (isOccupied) {
            throw new IllegalStateException("The property is already booked for the selected dates.");
        }
    }
}
