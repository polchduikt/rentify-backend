package com.rentify.core.dto.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.LocationSuggestionType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Location suggestion payload")
public record LocationSuggestionDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "Type", example = "CITY")
        LocationSuggestionType type,
        @Schema(description = "Name", example = "Sample value")
        String name,
        @JsonProperty("cityId") Long cityId,
        @JsonProperty("cityName") String cityName,
        @Schema(description = "Region", example = "Kyivska")
        String region,
        String country
) {
}
