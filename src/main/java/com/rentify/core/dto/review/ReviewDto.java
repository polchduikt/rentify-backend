package com.rentify.core.dto.review;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Review payload")
public record ReviewDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "Property id", example = "42")
        @JsonProperty("propertyId") Long propertyId,
        @Schema(description = "Booking id", example = "42")
        @JsonProperty("bookingId") Long bookingId,
        @Schema(description = "Author id", example = "42")
        @JsonProperty("authorId") Long authorId,
        @Schema(description = "Rating", example = "1")
        Short rating,
        @Schema(description = "Author first name", example = "Illia")
        @JsonProperty("authorFirstName") String authorFirstName,
        @Schema(description = "Comment", example = "Sample value")
        String comment,
        @Schema(description = "Created at", example = "2026-03-15T10:30:00+02:00")
        @JsonProperty("createdAt") ZonedDateTime createdAt
) {}
