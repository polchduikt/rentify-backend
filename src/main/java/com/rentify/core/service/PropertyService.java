package com.rentify.core.service;

import com.rentify.core.dto.PropertyCreateRequestDto;
import com.rentify.core.dto.PropertyResponseDto;
import java.util.List;

public interface PropertyService {
    List<PropertyResponseDto> getAllProperties();
    PropertyResponseDto getPropertyById(Long id);
    PropertyResponseDto create(PropertyCreateRequestDto request);
}