package com.rentify.core.service.impl;

import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.dto.property.PropertyPhotoDto;
import com.rentify.core.dto.property.PropertyResponseDto;
import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import com.rentify.core.entity.Amenity;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.PropertyPhoto;
import com.rentify.core.entity.User;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.mapper.PropertyMapper;
import com.rentify.core.repository.*;
import com.rentify.core.repository.specification.PropertySpecifications;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.CloudinaryService;
import com.rentify.core.service.PropertyService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
    private final CloudinaryService cloudinaryService;
    private final PropertyPhotoRepository propertyPhotoRepository;

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
        if (property.getRentalType() == null) {
            property.setRentalType(request.rentalType());
        }
        if (property.getPropertyType() == null) {
            property.setPropertyType(request.propertyType());
        }
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

    @Override
    @Transactional
    public PropertyPhotoDto uploadPhoto(Long propertyId, MultipartFile file) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        User currentUser = authenticationService.getCurrentUser();
        if (!property.getHost().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not the owner of this property. You cannot upload photos to it.");
        }
        String imageUrl = cloudinaryService.uploadFile(file);
        PropertyPhoto photo = PropertyPhoto.builder()
                .property(property)
                .url(imageUrl)
                .sortOrder(0)
                .build();
        PropertyPhoto savedPhoto = propertyPhotoRepository.save(photo);
        return new PropertyPhotoDto(
                savedPhoto.getId(),
                savedPhoto.getUrl(),
                savedPhoto.getSortOrder(),
                savedPhoto.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public PropertyResponseDto changePropertyStatus(Long id, PropertyStatus newStatus) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        User currentUser = authenticationService.getCurrentUser();
        boolean isHost = property.getHost().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        if (!isHost && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to change the status of this property");
        }
        if (newStatus == PropertyStatus.BLOCKED && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only administrators can block properties");
        }
        if (property.getStatus() == PropertyStatus.BLOCKED && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "This property is blocked by an administrator and cannot be unblocked manually");
        }
        property.setStatus(newStatus);
        return propertyMapper.toDto(propertyRepository.save(property));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponseDto> search(PropertySearchCriteriaDto criteria, Pageable pageable) {
        return propertyRepository.findAll(PropertySpecifications.withFilters(criteria), pageable)
                .map(propertyMapper::toDto);
    }
}
