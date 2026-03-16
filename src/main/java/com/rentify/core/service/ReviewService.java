package com.rentify.core.service;

import com.rentify.core.dto.review.ReviewDto;
import com.rentify.core.dto.review.ReviewRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewDto createReview(ReviewRequestDto request);
    Page<ReviewDto> getPropertyReviews(Long propertyId, Pageable pageable);
}