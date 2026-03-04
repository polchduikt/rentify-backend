package com.rentify.core.dto.review;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

public record ReviewDto(
        Long id,
        @JsonProperty("propertyId") Long propertyId,
        @JsonProperty("authorId") Long authorId,
        Short rating,
        @JsonProperty("authorFirstName") String authorFirstName,
        String comment,
        @JsonProperty("createdAt") ZonedDateTime createdAt
) {}
