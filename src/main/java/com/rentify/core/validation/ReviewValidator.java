package com.rentify.core.validation;

import com.rentify.core.dto.review.ReviewRequestDto;
import com.rentify.core.entity.Booking;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.enums.BookingStatus;
import jakarta.validation.Validator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ReviewValidator extends AbstractValidator {

    public ReviewValidator(Validator validator) {
        super(validator);
    }

    public void validateCreateReviewRequest(ReviewRequestDto request) {
        Set<String> errors = collectBeanErrors(request);

        if (request.propertyId() != null && request.propertyId() <= 0) {
            errors.add("propertyId: must be greater than 0");
        }
        if (request.bookingId() != null && request.bookingId() <= 0) {
            errors.add("bookingId: must be greater than 0");
        }

        throwIfAny(errors);
    }

    public void validateReviewEligibility(Booking booking, Property property, User author, boolean alreadyReviewed) {
        if (!booking.getTenant().getId().equals(author.getId())) {
            throw new AccessDeniedException("You can only review bookings that belong to you");
        }
        if (!booking.getProperty().getId().equals(property.getId())) {
            throw new IllegalArgumentException("Booking does not belong to the specified property");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("You can only review properties after your stay is COMPLETED");
        }
        if (alreadyReviewed) {
            throw new IllegalStateException("You have already reviewed this booking");
        }
    }
}
