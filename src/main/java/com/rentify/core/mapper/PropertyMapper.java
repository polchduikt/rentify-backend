package com.rentify.core.mapper;

import com.rentify.core.dto.property.AddressDto;
import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.dto.property.PropertyPhotoDto;
import com.rentify.core.dto.property.PropertyPricingDto;
import com.rentify.core.dto.property.PropertyResponseDto;
import com.rentify.core.dto.property.PropertyRuleDto;
import com.rentify.core.entity.Address;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.PropertyPhoto;
import com.rentify.core.entity.PropertyPricing;
import com.rentify.core.entity.PropertyRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PropertyMapper {
    @Mapping(source = "host.id", target = "hostId")
    PropertyResponseDto toDto(Property property);
    PropertyPhotoDto toPhotoDto(PropertyPhoto photo);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "host", ignore = true)
    Property toEntity(PropertyCreateRequestDto request);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PropertyPricing toPricingEntity(PropertyPricingDto dto);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "property", ignore = true)
    PropertyRule toRuleEntity(PropertyRuleDto dto);
    @Mapping(source = "cityRef.id", target = "cityId")
    @Mapping(source = "districtRef.id", target = "districtId")
    @Mapping(source = "metroStationRef.id", target = "metroStationId")
    @Mapping(source = "residentialComplexRef.id", target = "residentialComplexId")
    @Mapping(source = "districtRef.name", target = "districtName")
    @Mapping(source = "metroStationRef.name", target = "metroStationName")
    @Mapping(source = "residentialComplexRef.name", target = "residentialComplexName")
    AddressDto toAddressDto(Address address);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cityRef", ignore = true)
    @Mapping(target = "districtRef", ignore = true)
    @Mapping(target = "metroStationRef", ignore = true)
    @Mapping(target = "residentialComplexRef", ignore = true)
    Address toAddressEntity(AddressDto dto);
}
