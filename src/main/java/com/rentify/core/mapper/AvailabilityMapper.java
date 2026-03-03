package com.rentify.core.mapper;

import com.rentify.core.dto.AvailabilityBlockDto;
import com.rentify.core.entity.AvailabilityBlock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AvailabilityMapper {

    @Mapping(source = "property.id", target = "propertyId")
    AvailabilityBlockDto toDto(AvailabilityBlock entity);
}