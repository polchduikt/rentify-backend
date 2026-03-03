package com.rentify.core.service.impl;

import com.rentify.core.dto.ReviewDto;
import com.rentify.core.dto.ReviewRequestDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

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
        if (request.rating() < 1 || request.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        User author = authService.getCurrentUser();
        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        boolean hasStayed = bookingRepository.existsByTenantIdAndPropertyIdAndStatus(
                author.getId(),
                property.getId(),
                BookingStatus.COMPLETED
        );
        if (!hasStayed) {
            throw new IllegalStateException("You can only review properties after your stay is COMPLETED.");
        }
        if (reviewRepository.existsByAuthorIdAndPropertyId(author.getId(), property.getId())) {
            throw new IllegalStateException("You have already reviewed this property.");
        }
        Review review = Review.builder()
                .property(property)
                .author(author)
                .rating(request.rating())
                .comment(request.comment())
                .build();
        return reviewMapper.toDto(reviewRepository.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getPropertyReviews(Long propertyId, Pageable pageable) {
        return reviewRepository.findAllByPropertyId(propertyId, pageable)
                .map(reviewMapper::toDto);
    }
}