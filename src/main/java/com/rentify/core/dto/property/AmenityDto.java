package com.rentify.core.dto.property;

import com.rentify.core.enums.AmenityCategory;

public record AmenityDto(
        Long id,
        String name,
        AmenityCategory category,
        String slug,
        String icon
) {}
