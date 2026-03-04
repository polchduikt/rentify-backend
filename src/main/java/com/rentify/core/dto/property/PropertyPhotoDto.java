package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

public record PropertyPhotoDto(
        Long id,
        String url,
        @JsonProperty("sortOrder") Integer sortOrder,
        @JsonProperty("createdAt") ZonedDateTime createdAt
) {}
