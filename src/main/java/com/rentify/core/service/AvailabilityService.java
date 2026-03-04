package com.rentify.core.service;

import com.rentify.core.dto.property.AvailabilityBlockDto;
import com.rentify.core.dto.property.AvailabilityBlockRequestDto;
import java.util.List;

public interface AvailabilityService {
    AvailabilityBlockDto createBlock(Long propertyId, AvailabilityBlockRequestDto request);
    List<AvailabilityBlockDto> getBlocksByProperty(Long propertyId);
    void deleteBlock(Long propertyId, Long blockId);
}