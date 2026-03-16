package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.AmenityCategory;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Amenity category group payload")
public record AmenityCategoryGroupDto(
        @Schema(description = "Category", example = "BASIC")
        AmenityCategory category,
        @JsonProperty("amenities") List<AmenityDto> amenities
) {
}
