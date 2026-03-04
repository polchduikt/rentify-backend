package com.rentify.core.dto.review;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequestDto(
        @NotNull(message = "Property id is required")
        @JsonProperty("propertyId")
        Long propertyId,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be from 1 to 5")
        @Max(value = 5, message = "Rating must be from 1 to 5")
        @JsonProperty("rating")
        Short rating,

        @Size(max = 2000, message = "Comment is too long")
        @JsonProperty("comment")
        String comment
) {}
