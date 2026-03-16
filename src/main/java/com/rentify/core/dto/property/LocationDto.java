package com.rentify.core.dto.property;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Location payload")
public record LocationDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @NotBlank(message = "Country is required")
        @Schema(description = "Country", example = "Ukraine")
        String country,
        @Schema(description = "Region", example = "Kyivska")
        String region,
        @NotBlank(message = "City is required")
        @Schema(description = "City", example = "Kyiv")
        String city
) {}
