package com.rentify.core.service.impl;

import com.rentify.core.dto.review.ReviewDto;
import com.rentify.core.dto.review.ReviewRequestDto;
import com.rentify.core.entity.Booking;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.Review;
import com.rentify.core.entity.User;
import com.rentify.core.exception.DomainException;
import com.rentify.core.mapper.ReviewMapper;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.repository.ReviewRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.ReviewService;
import com.rentify.core.validation.ReviewValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final AuthenticationService authService;
    private final ReviewMapper reviewMapper;
    private final ReviewValidator reviewValidator;

    @Override
    @Transactional
    public ReviewDto createReview(ReviewRequestDto request) {
        reviewValidator.validateCreateReviewRequest(request);

        User author = authService.getCurrentUser();
        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        Property property = propertyRepository.findByIdForUpdate(request.propertyId())
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        boolean alreadyReviewed = reviewRepository.existsByBookingId(booking.getId());
        reviewValidator.validateReviewEligibility(booking, property, author, alreadyReviewed);

        Review review = Review.builder()
                .property(property)
                .booking(booking)
                .author(author)
                .rating(request.rating())
                .comment(request.comment())
                .build();
        Review savedReview;
        try {
            savedReview = reviewRepository.save(review);
        } catch (DataIntegrityViolationException ex) {
            throw DomainException.conflict("REVIEW_ALREADY_EXISTS", "You have already reviewed this booking");
        }

        long reviewCount = reviewRepository.countByPropertyId(property.getId());
        BigDecimal averageRating = reviewRepository.findAverageRatingByPropertyId(property.getId());
        property.setReviewCount(reviewCount);
        property.setAverageRating((averageRating == null ? BigDecimal.ZERO : averageRating)
                .setScale(2, RoundingMode.HALF_UP));
        propertyRepository.save(property);

        return reviewMapper.toDto(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getPropertyReviews(Long propertyId, Pageable pageable) {
        return reviewRepository.findAllByPropertyId(propertyId, pageable)
                .map(reviewMapper::toDto);
    }
}
