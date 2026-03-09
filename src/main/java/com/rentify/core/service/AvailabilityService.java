package com.rentify.core.service;

import com.rentify.core.dto.property.AvailabilityBlockDto;
import com.rentify.core.dto.property.AvailabilityBlockRequestDto;
import com.rentify.core.dto.property.UnavailableDateRangeDto;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityService {
    AvailabilityBlockDto createBlock(Long propertyId, AvailabilityBlockRequestDto request);
    List<AvailabilityBlockDto> getBlocksByProperty(Long propertyId);
    List<UnavailableDateRangeDto> getUnavailableRangesByProperty(Long propertyId, LocalDate dateFrom, LocalDate dateTo);
    void deleteBlock(Long propertyId, Long blockId);
}
