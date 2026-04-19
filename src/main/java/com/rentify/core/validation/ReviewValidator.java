package com.rentify.core.validation;

import com.rentify.core.dto.review.ReviewRequestDto;
import com.rentify.core.entity.Booking;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.enums.BookingStatus;
import com.rentify.core.exception.DomainException;
import jakarta.validation.Validator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReviewValidator extends AbstractValidator {

    public ReviewValidator(Validator validator) {
        super(validator);
    }

    public void validateCreateReviewRequest(ReviewRequestDto request) {
        Map<String, String> errors = collectBeanErrors(request);

        if (request.propertyId() != null && request.propertyId() <= 0) {
            errors.put("propertyId", "must be greater than 0");
        }
        if (request.bookingId() != null && request.bookingId() <= 0) {
            errors.put("bookingId", "must be greater than 0");
        }

        throwIfAny(errors);
    }

    public void validateReviewEligibility(Booking booking, Property property, User author, boolean alreadyReviewed) {
        if (!booking.getTenant().getId().equals(author.getId())) {
            throw new AccessDeniedException("You can only review bookings that belong to you");
        }
        if (!booking.getProperty().getId().equals(property.getId())) {
            throw DomainException.badRequest("REVIEW_BOOKING_PROPERTY_MISMATCH", "Booking does not belong to the specified property");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw DomainException.conflict("REVIEW_NOT_ALLOWED_YET", "You can only review properties after your stay is COMPLETED");
        }
        if (alreadyReviewed) {
            throw DomainException.conflict("REVIEW_ALREADY_EXISTS", "You have already reviewed this booking");
        }
    }
}
