package com.rentify.core.service.impl;

import com.rentify.core.dto.review.ReviewDto;
import com.rentify.core.dto.review.ReviewRequestDto;
import com.rentify.core.entity.Booking;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.Review;
import com.rentify.core.entity.User;
import com.rentify.core.enums.BookingStatus;
import com.rentify.core.mapper.ReviewMapper;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.repository.ReviewRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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

    @Override
    @Transactional
    public ReviewDto createReview(ReviewRequestDto request) {
        User author = authService.getCurrentUser();
        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        Property property = propertyRepository.findByIdForUpdate(request.propertyId())
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        if (!booking.getTenant().getId().equals(author.getId())) {
            throw new AccessDeniedException("You can only review bookings that belong to you");
        }
        if (!booking.getProperty().getId().equals(property.getId())) {
            throw new IllegalArgumentException("Booking does not belong to the specified property");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("You can only review properties after your stay is COMPLETED");
        }
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new IllegalStateException("You have already reviewed this booking");
        }

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
            throw new IllegalStateException("You have already reviewed this booking");
        }

        long reviewCount = reviewRepository.countByPropertyId(property.getId());
        Double averageRating = reviewRepository.findAverageRatingByPropertyId(property.getId());
        property.setReviewCount(reviewCount);
        property.setAverageRating(BigDecimal.valueOf(averageRating == null ? 0d : averageRating)
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
