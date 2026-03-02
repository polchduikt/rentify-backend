package com.rentify.core.service;

import com.rentify.core.dto.ReviewDto;
import com.rentify.core.dto.ReviewRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewDto createReview(ReviewRequestDto request);
    Page<ReviewDto> getPropertyReviews(Long propertyId, Pageable pageable);
}