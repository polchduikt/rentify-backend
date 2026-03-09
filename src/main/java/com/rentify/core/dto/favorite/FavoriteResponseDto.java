package com.rentify.core.dto.favorite;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.dto.property.PropertyResponseDto;

import java.time.ZonedDateTime;

public record FavoriteResponseDto(
        Long id,
        @JsonProperty("propertyId") Long propertyId,
        @JsonProperty("createdAt") ZonedDateTime createdAt,
        PropertyResponseDto property
) {
}
