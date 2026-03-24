package com.rentify.core.mapper;

import com.rentify.core.dto.property.AddressDto;
import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.dto.property.PropertyMapPinDto;
import com.rentify.core.dto.property.PropertyPhotoDto;
import com.rentify.core.dto.property.PropertyPricingDto;
import com.rentify.core.dto.property.PropertyResponseDto;
import com.rentify.core.dto.property.PropertyRuleDto;
import com.rentify.core.entity.Address;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.PropertyPhoto;
import com.rentify.core.entity.PropertyPricing;
import com.rentify.core.entity.PropertyRule;
import com.rentify.core.enums.RentalType;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.List;

@Mapper(config = MapStructCentralConfig.class)
public interface PropertyMapper {
    @Mapping(source = "host.id", target = "hostId")
    PropertyResponseDto toDto(Property property);
    List<PropertyResponseDto> toDtos(List<Property> properties);
    PropertyPhotoDto toPhotoDto(PropertyPhoto photo);
    List<PropertyPhotoDto> toPhotoDtos(List<PropertyPhoto> photos);

    @Mapping(source = "address.lat", target = "lat")
    @Mapping(source = "address.lng", target = "lng")
    @Mapping(source = "pricing.currency", target = "currency")
    @Mapping(target = "price", expression = "java(resolveMapPrice(property))")
    PropertyMapPinDto toMapPinDto(Property property);

    List<PropertyMapPinDto> toMapPinDtos(List<Property> properties);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "host", ignore = true)
    @Mapping(target = "isTopPromoted", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "topPromotedUntil", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    Property toEntity(PropertyCreateRequestDto request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "rentalType", target = "rentalType")
    @Mapping(source = "propertyType", target = "propertyType")
    @Mapping(source = "marketType", target = "marketType")
    @Mapping(source = "rooms", target = "rooms")
    @Mapping(source = "floor", target = "floor")
    @Mapping(source = "totalFloors", target = "totalFloors")
    @Mapping(source = "areaSqm", target = "areaSqm")
    @Mapping(source = "maxGuests", target = "maxGuests")
    @Mapping(source = "checkInTime", target = "checkInTime")
    @Mapping(source = "checkOutTime", target = "checkOutTime")
    void updateEntity(PropertyCreateRequestDto request, @MappingTarget Property property);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "property", ignore = true)
    PropertyPricing toPricingEntity(PropertyPricingDto dto);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "pricePerNight", target = "pricePerNight")
    @Mapping(source = "pricePerMonth", target = "pricePerMonth")
    @Mapping(source = "currency", target = "currency")
    @Mapping(source = "securityDeposit", target = "securityDeposit")
    @Mapping(source = "cleaningFee", target = "cleaningFee")
    void updatePricing(PropertyPricingDto dto, @MappingTarget PropertyPricing pricing);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "property", ignore = true)
    PropertyRule toRuleEntity(PropertyRuleDto dto);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "petsAllowed", target = "petsAllowed")
    @Mapping(source = "smokingAllowed", target = "smokingAllowed")
    @Mapping(source = "partiesAllowed", target = "partiesAllowed")
    @Mapping(source = "additionalRules", target = "additionalRules")
    void updateRules(PropertyRuleDto dto, @MappingTarget PropertyRule rules);

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

    default BigDecimal resolveMapPrice(Property property) {
        PropertyPricing pricing = property.getPricing();
        if (pricing == null) {
            return null;
        }
        if (property.getRentalType() == RentalType.SHORT_TERM) {
            return pricing.getPricePerNight() != null ? pricing.getPricePerNight() : pricing.getPricePerMonth();
        }
        if (property.getRentalType() == RentalType.LONG_TERM) {
            return pricing.getPricePerMonth() != null ? pricing.getPricePerMonth() : pricing.getPricePerNight();
        }
        return pricing.getPricePerNight() != null ? pricing.getPricePerNight() : pricing.getPricePerMonth();
    }
}
