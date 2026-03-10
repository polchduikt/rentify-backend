package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.AmenityCategory;

import java.util.List;

public record AmenityCategoryGroupDto(
        AmenityCategory category,
        @JsonProperty("amenities") List<AmenityDto> amenities
) {
}
