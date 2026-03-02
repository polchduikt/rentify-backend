package com.rentify.core.service;

import com.rentify.core.dto.PropertyCreateRequestDto;
import com.rentify.core.dto.PropertyResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PropertyService {
    Page<PropertyResponseDto> getAllProperties(Pageable pageable);
    PropertyResponseDto getPropertyById(Long id);
    PropertyResponseDto create(PropertyCreateRequestDto request);
}