package com.rentify.core.service.impl;

import com.rentify.core.dto.PropertyCreateRequestDto;
import com.rentify.core.dto.PropertyResponseDto;
import com.rentify.core.entity.Amenity;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.mapper.PropertyMapper;
import com.rentify.core.repository.*;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.PropertyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final AmenityRepository amenityRepository;
    private final PropertyMapper propertyMapper;
    private final AddressRepository addressRepository;
    private final LocationRepository locationRepository;
    private final AuthenticationService authenticationService;

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponseDto> getAllProperties(Pageable pageable) {
        Page<Property> propertiesPage = propertyRepository.findAll(pageable);
        return propertiesPage.map(propertyMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyResponseDto getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        return propertyMapper.toDto(property);
    }

    @Override
    @Transactional
    public PropertyResponseDto create(PropertyCreateRequestDto request) {
        User host = authenticationService.getCurrentUser();
        Property property = propertyMapper.toEntity(request);
        property.setHost(host);
        property.setStatus(PropertyStatus.ACTIVE);
        if (property.getPricing() != null) {
            property.getPricing().setProperty(property);
        }
        if (property.getRules() != null) {
            property.getRules().setProperty(property);
        }
        if (request.amenityIds() != null && !request.amenityIds().isEmpty()) {
            List<Amenity> foundAmenities = amenityRepository.findAllById(request.amenityIds());
            if (foundAmenities.size() != request.amenityIds().size()) {
                throw new EntityNotFoundException("One or more amenities provided in the request do not exist.");
            }
            property.setAmenities(new HashSet<>(foundAmenities));
        }
        if (property.getAddress() != null) {
            if (property.getAddress().getLocation() != null) {
                com.rentify.core.entity.Location savedLocation =
                        locationRepository.save(property.getAddress().getLocation());
                property.getAddress().setLocation(savedLocation);
            }
            com.rentify.core.entity.Address savedAddress =
                    addressRepository.save(property.getAddress());
            property.setAddress(savedAddress);
        }
        Property savedProperty = propertyRepository.save(property);
        return propertyMapper.toDto(savedProperty);
    }
}