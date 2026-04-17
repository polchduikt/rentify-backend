package com.rentify.core.service.impl;

import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.dto.property.PropertyMapPinDto;
import com.rentify.core.dto.property.PropertyPhotoDto;
import com.rentify.core.dto.property.PropertyResponseDto;
import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import com.rentify.core.entity.Amenity;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.PropertyPricing;
import com.rentify.core.entity.PropertyRule;
import com.rentify.core.entity.User;
import com.rentify.core.enums.AmenityCategory;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.RentalType;
import com.rentify.core.mapper.PropertyMapper;
import com.rentify.core.repository.AmenityRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.security.UserRoleUtils;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.PropertyService;
import com.rentify.core.service.impl.property.PropertyAddressService;
import com.rentify.core.service.impl.property.PropertyCleanupService;
import com.rentify.core.service.impl.property.PropertyPhotoService;
import com.rentify.core.service.impl.property.PropertySearchService;
import com.rentify.core.validation.PropertyValidator;
import com.rentify.core.exception.DomainException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final AmenityRepository amenityRepository;
    private final PropertyMapper propertyMapper;
    private final AuthenticationService authenticationService;
    private final PropertyValidator propertyValidator;
    private final PropertyAddressService propertyAddressService;
    private final PropertyPhotoService propertyPhotoService;
    private final PropertySearchService propertySearchService;
    private final PropertyCleanupService propertyCleanupService;

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponseDto> getAllProperties(Pageable pageable) {
        Page<Property> propertiesPage = propertyRepository.findAll(withTopPrioritySort(pageable));
        return propertiesPage.map(propertyMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponseDto> getCurrentUserProperties(Pageable pageable, List<PropertyStatus> statuses) {
        User currentUser = authenticationService.getCurrentUser();
        Pageable sortedPageable = withTopPrioritySort(pageable);
        List<PropertyStatus> effectiveStatuses = (statuses == null || statuses.isEmpty())
                ? List.of(PropertyStatus.values())
                : statuses;
        return propertyRepository.findAllByHostIdAndStatusIn(currentUser.getId(), effectiveStatuses, sortedPageable)
                .map(propertyMapper::toDto);
    }

    @Override
    @Transactional
    public PropertyResponseDto getPropertyById(Long id) {
        int updatedRows = propertyRepository.incrementViewCount(id);
        if (updatedRows == 0) {
            throw new EntityNotFoundException("Property not found");
        }
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        return propertyMapper.toDto(property);
    }

    @Override
    @Transactional
    public PropertyResponseDto create(PropertyCreateRequestDto request) {
        propertyValidator.validateCreateOrUpdateRequest(request);
        assertRentalPricingRules(request);
        User host = authenticationService.getCurrentUser();
        Property property = propertyMapper.toEntity(request);
        property.setRentalType(request.rentalType());
        property.setPropertyType(request.propertyType());
        property.setMarketType(request.marketType());
        applyListingFlags(property, request, host);
        property.setHost(host);
        property.setStatus(PropertyStatus.DRAFT);
        if (property.getPricing() != null) {
            property.getPricing().setProperty(property);
        }
        if (property.getRules() != null) {
            property.getRules().setProperty(property);
        }
        updateAmenities(property, request.amenityIds(), request.amenitySlugs(), host);
        propertyAddressService.updateAddress(property, request);
        Property savedProperty = propertyRepository.save(property);
        return propertyMapper.toDto(savedProperty);
    }

    @Override
    @Transactional
    public PropertyResponseDto updateProperty(Long id, PropertyCreateRequestDto request) {
        propertyValidator.validateCreateOrUpdateRequest(request);
        assertRentalPricingRules(request);
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        User currentUser = authenticationService.getCurrentUser();
        assertCanManageProperty(property, currentUser);
        propertyMapper.updateEntity(request, property);
        property.setRentalType(request.rentalType());
        property.setPropertyType(request.propertyType());
        applyListingFlags(property, request, currentUser);
        updateAmenities(property, request.amenityIds(), request.amenitySlugs(), currentUser);
        propertyAddressService.updateAddress(property, request);
        updatePricing(property, request);
        updateRules(property, request);
        Property updatedProperty = propertyRepository.save(property);
        return propertyMapper.toDto(updatedProperty);
    }

    @Override
    @Transactional
    public void deleteProperty(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        User currentUser = authenticationService.getCurrentUser();
        assertCanManageProperty(property, currentUser);
        propertyCleanupService.cleanupBeforeDelete(id);
        propertyRepository.delete(property);
    }

    @Override
    @Transactional
    public PropertyPhotoDto uploadPhoto(Long propertyId, MultipartFile file) {
        return propertyPhotoService.uploadPhoto(propertyId, file);
    }

    @Override
    @Transactional
    public void deletePhoto(Long propertyId, Long photoId) {
        propertyPhotoService.deletePhoto(propertyId, photoId);
    }

    @Override
    @Transactional
    public PropertyResponseDto changePropertyStatus(Long id, PropertyStatus newStatus) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        User currentUser = authenticationService.getCurrentUser();
        boolean isHost = property.getHost().getId().equals(currentUser.getId());
        boolean isAdmin = UserRoleUtils.isAdmin(currentUser);
        if (!isHost && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to change the status of this property");
        }
        if (newStatus == PropertyStatus.BLOCKED && !isAdmin) {
            throw new AccessDeniedException("Only administrators can block properties");
        }
        if (property.getStatus() == PropertyStatus.BLOCKED && !isAdmin) {
            throw new AccessDeniedException(
                    "This property is blocked by an administrator and cannot be unblocked manually");
        }
        property.setStatus(newStatus);
        return propertyMapper.toDto(propertyRepository.save(property));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponseDto> search(PropertySearchCriteriaDto criteria, Pageable pageable) {
        return propertySearchService.search(criteria, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyMapPinDto> searchMapPins(PropertySearchCriteriaDto criteria, Pageable pageable) {
        return propertySearchService.searchMapPins(criteria, pageable);
    }

    private void updateAmenities(
            Property property,
            List<Long> amenityIds,
            List<String> amenitySlugs,
            User currentUser
    ) {
        boolean hasAmenityIds = amenityIds != null && !amenityIds.isEmpty();
        List<String> normalizedSlugs = normalizeSlugs(amenitySlugs);
        boolean hasAmenitySlugs = !normalizedSlugs.isEmpty();
        if (!hasAmenityIds && !hasAmenitySlugs) {
            property.setAmenities(new HashSet<>());
            return;
        }

        Set<Amenity> amenities = new HashSet<>();

        if (hasAmenityIds) {
            List<Amenity> foundByIds = amenityRepository.findAllById(amenityIds);
            if (foundByIds.size() != amenityIds.size()) {
                throw new EntityNotFoundException("One or more amenities provided in the request do not exist.");
            }
            amenities.addAll(foundByIds);
        }

        if (hasAmenitySlugs) {
            List<Amenity> foundBySlugs = amenityRepository.findAllBySlugInIgnoreCase(normalizedSlugs);
            if (foundBySlugs.size() != normalizedSlugs.size()) {
                throw new EntityNotFoundException("One or more amenity slugs provided in the request do not exist.");
            }
            amenities.addAll(foundBySlugs);
        }

        if (!UserRoleUtils.isAdmin(currentUser)) {
            Set<Amenity> currentAmenities = property.getAmenities() != null ? property.getAmenities() : Set.of();
            Set<Amenity> existingVerificationAmenities = currentAmenities.stream()
                    .filter(amenity -> amenity.getCategory() == AmenityCategory.VERIFICATION)
                    .collect(Collectors.toSet());
            amenities.removeIf(amenity -> amenity.getCategory() == AmenityCategory.VERIFICATION);
            amenities.addAll(existingVerificationAmenities);
        }

        property.setAmenities(amenities);
    }

    private void updatePricing(Property property, PropertyCreateRequestDto request) {
        if (request.pricing() == null) {
            return;
        }
        if (property.getPricing() == null) {
            PropertyPricing pricing = propertyMapper.toPricingEntity(request.pricing());
            pricing.setProperty(property);
            property.setPricing(pricing);
            return;
        }
        propertyMapper.updatePricing(request.pricing(), property.getPricing());
    }

    private void updateRules(Property property, PropertyCreateRequestDto request) {
        if (request.rules() == null) {
            return;
        }
        if (property.getRules() == null) {
            PropertyRule rules = propertyMapper.toRuleEntity(request.rules());
            rules.setProperty(property);
            property.setRules(rules);
            return;
        }
        propertyMapper.updateRules(request.rules(), property.getRules());
    }

    private void applyListingFlags(Property property, PropertyCreateRequestDto request, User currentUser) {
        if (!UserRoleUtils.isAdmin(currentUser)) {
            return;
        }
        property.setIsVerifiedProperty(Boolean.TRUE.equals(request.isVerifiedProperty()));
        property.setIsVerifiedRealtor(Boolean.TRUE.equals(request.isVerifiedRealtor()));
        property.setIsDuplicate(Boolean.TRUE.equals(request.isDuplicate()));
    }

    private List<String> normalizeSlugs(List<String> amenitySlugs) {
        if (amenitySlugs == null || amenitySlugs.isEmpty()) {
            return List.of();
        }
        Set<String> normalized = new HashSet<>();
        for (String slug : amenitySlugs) {
            if (slug == null || slug.isBlank()) {
                continue;
            }
            normalized.add(slug.trim().toLowerCase(Locale.ROOT));
        }
        return new ArrayList<>(normalized);
    }

    private void assertCanManageProperty(Property property, User currentUser) {
        boolean isHost = property.getHost().getId().equals(currentUser.getId());
        boolean isAdmin = UserRoleUtils.isAdmin(currentUser);
        if (!isHost && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to manage this property");
        }
    }

    private void assertRentalPricingRules(PropertyCreateRequestDto request) {
        if (request.rentalType() == RentalType.SHORT_TERM) {
            if (request.maxGuests() == null) {
                throw DomainException.badRequest(
                        "RENTAL_PRICING_INVALID",
                        "maxGuests is required for short-term rental",
                        java.util.Map.of("maxGuests", "required")
                );
            }
            if (request.pricing() == null || request.pricing().pricePerNight() == null
                    || request.pricing().pricePerNight().compareTo(BigDecimal.ZERO) <= 0) {
                throw DomainException.badRequest(
                        "RENTAL_PRICING_INVALID",
                        "pricePerNight is required and must be greater than 0 for short-term rental",
                        java.util.Map.of("pricePerNight", "must be greater than 0")
                );
            }
        }
        if (request.rentalType() == RentalType.LONG_TERM) {
            if (request.pricing() == null || request.pricing().pricePerMonth() == null
                    || request.pricing().pricePerMonth().compareTo(BigDecimal.ZERO) <= 0) {
                throw DomainException.badRequest(
                        "RENTAL_PRICING_INVALID",
                        "pricePerMonth is required and must be greater than 0 for long-term rental",
                        java.util.Map.of("pricePerMonth", "must be greater than 0")
                );
            }
        }
    }

    private Pageable withTopPrioritySort(Pageable pageable) {
        Sort topPrioritySort = Sort.by(
                Sort.Order.desc("isTopPromoted"),
                Sort.Order.desc("topPromotedUntil")
        );
        Sort finalSort = pageable.getSort().isSorted()
                ? topPrioritySort.and(pageable.getSort())
                : topPrioritySort.and(Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), finalSort);
    }
}
