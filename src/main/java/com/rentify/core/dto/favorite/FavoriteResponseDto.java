package com.rentify.core.dto.favorite;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.dto.property.PropertyResponseDto;

import java.time.ZonedDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Favorite response payload")
public record FavoriteResponseDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @JsonProperty("propertyId") Long propertyId,
        @JsonProperty("createdAt") ZonedDateTime createdAt,
        PropertyResponseDto property
) {
}
