package com.rentify.core.controller;

import com.rentify.core.dto.review.ReviewDto;
import com.rentify.core.dto.review.ReviewRequestDto;
import com.rentify.core.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto> create(@Valid @RequestBody ReviewRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(request));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<Page<ReviewDto>> getPropertyReviews(@PathVariable Long propertyId, Pageable pageable) {
        return ResponseEntity.ok(reviewService.getPropertyReviews(propertyId, pageable));
    }
}
