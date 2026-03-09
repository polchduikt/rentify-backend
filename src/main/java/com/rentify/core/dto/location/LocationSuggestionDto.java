package com.rentify.core.dto.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.LocationSuggestionType;

public record LocationSuggestionDto(
        Long id,
        LocationSuggestionType type,
        String name,
        @JsonProperty("cityId") Long cityId,
        @JsonProperty("cityName") String cityName,
        String region,
        String country
) {
}
