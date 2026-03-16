package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Property photo payload")
public record PropertyPhotoDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "Url", example = "https://example.com/resource.jpg")
        String url,
        @Schema(description = "Sort order", example = "1")
        @JsonProperty("sortOrder") Integer sortOrder,
        @Schema(description = "Created at", example = "2026-03-15T10:30:00+02:00")
        @JsonProperty("createdAt") ZonedDateTime createdAt
) {}
