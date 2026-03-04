package com.rentify.core.dto.property;

import jakarta.validation.constraints.NotBlank;

public record LocationDto(
        Long id,
        @NotBlank(message = "Country is required")
        String country,
        String region,
        @NotBlank(message = "City is required")
        String city
) {}
