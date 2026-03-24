package com.rentify.core.mapper;

import com.rentify.core.dto.property.AmenityCategoryGroupDto;
import com.rentify.core.dto.property.AmenityDto;
import com.rentify.core.entity.Amenity;
import com.rentify.core.enums.AmenityCategory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MapStructCentralConfig.class)
public interface AmenityMapper {
    AmenityDto toDto(Amenity amenity);

    List<AmenityDto> toDtos(List<Amenity> amenities);

    default AmenityCategoryGroupDto toCategoryGroupDto(AmenityCategory category, List<AmenityDto> amenities) {
        return new AmenityCategoryGroupDto(category, amenities);
    }
}
