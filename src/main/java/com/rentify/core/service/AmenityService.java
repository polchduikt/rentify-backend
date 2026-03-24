package com.rentify.core.service;

import com.rentify.core.dto.property.AmenityCategoryGroupDto;
import com.rentify.core.dto.property.AmenityDto;
import com.rentify.core.enums.AmenityCategory;

import java.util.List;

public interface AmenityService {
    List<AmenityDto> getAmenities(AmenityCategory category);
    List<AmenityCategoryGroupDto> getAmenitiesGrouped();
}
