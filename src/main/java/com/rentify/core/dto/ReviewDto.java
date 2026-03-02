package com.rentify.core.dto;

import java.time.ZonedDateTime;

public record ReviewDto(
        Long id,
        Long propertyId,
        Long authorId,
        Short rating,
        String authorFirstName,
        String comment,
        ZonedDateTime createdAt
) {}
