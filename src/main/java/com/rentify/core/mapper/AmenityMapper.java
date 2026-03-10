package com.rentify.core.mapper;

import com.rentify.core.dto.property.AmenityDto;
import com.rentify.core.entity.Amenity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AmenityMapper {
    AmenityDto toDto(Amenity amenity);

    List<AmenityDto> toDtos(List<Amenity> amenities);
}
