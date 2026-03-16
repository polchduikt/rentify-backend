package com.rentify.core.mapper;

import com.rentify.core.dto.booking.BookingDto;
import com.rentify.core.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(source = "property.id", target = "propertyId")
    @Mapping(source = "tenant.id", target = "tenantId")
    BookingDto toDto(Booking booking);
}

