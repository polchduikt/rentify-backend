package com.rentify.core.mapper;

import com.rentify.core.dto.property.AvailabilityBlockDto;
import com.rentify.core.dto.property.UnavailableDateRangeDto;
import com.rentify.core.entity.AvailabilityBlock;
import com.rentify.core.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructCentralConfig.class)
public interface AvailabilityMapper {

    @Mapping(source = "property.id", target = "propertyId")
    @Mapping(source = "createdBy.id", target = "createdById")
    AvailabilityBlockDto toDto(AvailabilityBlock entity);

    List<AvailabilityBlockDto> toDtos(List<AvailabilityBlock> entities);

    @Mapping(source = "dateFrom", target = "dateFrom")
    @Mapping(source = "dateTo", target = "dateTo")
    @Mapping(target = "source", constant = "BLOCK")
    @Mapping(target = "bookingStatus", ignore = true)
    UnavailableDateRangeDto toUnavailableDateRangeDto(AvailabilityBlock block);

    List<UnavailableDateRangeDto> toUnavailableDateRangeDtosFromBlocks(List<AvailabilityBlock> blocks);

    @Mapping(source = "dateFrom", target = "dateFrom")
    @Mapping(source = "dateTo", target = "dateTo")
    @Mapping(target = "source", constant = "BOOKING")
    @Mapping(source = "status", target = "bookingStatus")
    UnavailableDateRangeDto toUnavailableDateRangeDto(Booking booking);

    List<UnavailableDateRangeDto> toUnavailableDateRangeDtosFromBookings(List<Booking> bookings);
}
