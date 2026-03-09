package com.rentify.core.service;

import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.dto.property.PropertyPhotoDto;
import com.rentify.core.dto.property.PropertyResponseDto;
import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface PropertyService {
    Page<PropertyResponseDto> getAllProperties(Pageable pageable);
    Page<PropertyResponseDto> getCurrentUserProperties(Pageable pageable);
    PropertyResponseDto getPropertyById(Long id);
    PropertyResponseDto create(PropertyCreateRequestDto request);
    PropertyResponseDto updateProperty(Long id, PropertyCreateRequestDto request);
    void deleteProperty(Long id);
    PropertyPhotoDto uploadPhoto(Long propertyId, MultipartFile file);
    void deletePhoto(Long propertyId, Long photoId);
    Page<PropertyResponseDto> search(PropertySearchCriteriaDto criteria, Pageable pageable);
    PropertyResponseDto changePropertyStatus(Long id, com.rentify.core.enums.PropertyStatus newStatus);
}
