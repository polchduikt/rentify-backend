package com.rentify.core.service;

import com.rentify.core.dto.PropertyCreateRequestDto;
import com.rentify.core.dto.PropertyPhotoDto;
import com.rentify.core.dto.PropertyResponseDto;
import com.rentify.core.dto.PropertySearchCriteriaDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface PropertyService {
    Page<PropertyResponseDto> getAllProperties(Pageable pageable);
    PropertyResponseDto getPropertyById(Long id);
    PropertyResponseDto create(PropertyCreateRequestDto request);
    PropertyPhotoDto uploadPhoto(Long propertyId, MultipartFile file);
    Page<PropertyResponseDto> search(PropertySearchCriteriaDto criteria, Pageable pageable);
}