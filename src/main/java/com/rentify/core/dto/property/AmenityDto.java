package com.rentify.core.dto.property;

import com.rentify.core.enums.AmenityCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Amenity payload")
public record AmenityDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "Name", example = "Sample value")
        String name,
        @Schema(description = "Category", example = "BASIC")
        AmenityCategory category,
        @Schema(description = "Slug", example = "wifi")
        String slug,
        @Schema(description = "Icon", example = "Sample value")
        String icon
) {}
