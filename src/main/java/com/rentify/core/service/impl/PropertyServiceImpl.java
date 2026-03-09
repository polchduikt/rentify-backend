package com.rentify.core.service.impl;

import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.dto.property.PropertyPhotoDto;
import com.rentify.core.dto.property.PropertyResponseDto;
import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import com.rentify.core.entity.Address;
import com.rentify.core.entity.Amenity;
import com.rentify.core.entity.Location;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.PropertyPhoto;
import com.rentify.core.entity.PropertyPricing;
import com.rentify.core.entity.PropertyRule;
import com.rentify.core.entity.User;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.mapper.PropertyMapper;
import com.rentify.core.repository.AddressRepository;
import com.rentify.core.repository.AmenityRepository;
import com.rentify.core.repository.AvailabilityBlockRepository;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.ConversationRepository;
import com.rentify.core.repository.FavoriteRepository;
import com.rentify.core.repository.LocationRepository;
import com.rentify.core.repository.LongTermRequestRepository;
import com.rentify.core.repository.PropertyPhotoRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.repository.ReviewRepository;
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
    private final AvailabilityBlockRepository availabilityBlockRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final ConversationRepository conversationRepository;
    private final LongTermRequestRepository longTermRequestRepository;
    private final FavoriteRepository favoriteRepository;

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
    public PropertyResponseDto updateProperty(Long id, PropertyCreateRequestDto request) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        User currentUser = authenticationService.getCurrentUser();
        assertCanManageProperty(property, currentUser);
        property.setTitle(request.title());
        property.setDescription(request.description());
        property.setRentalType(request.rentalType());
        property.setPropertyType(request.propertyType());
        property.setRooms(request.rooms());
        property.setFloor(request.floor());
        property.setTotalFloors(request.totalFloors());
        property.setAreaSqm(request.areaSqm());
        property.setMaxGuests(request.maxGuests());
        property.setCheckInTime(request.checkInTime());
        property.setCheckOutTime(request.checkOutTime());
        updateAmenities(property, request.amenityIds());
        updateAddress(property, request);
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
        if (bookingRepository.existsByPropertyId(id)) {
            throw new IllegalStateException("Property cannot be deleted because it has bookings");
        }
        if (reviewRepository.existsByPropertyId(id)) {
            throw new IllegalStateException("Property cannot be deleted because it has reviews");
        }
        if (conversationRepository.existsByPropertyId(id)) {
            throw new IllegalStateException("Property cannot be deleted because it has conversations");
        }
        if (longTermRequestRepository.existsByPropertyId(id)) {
            throw new IllegalStateException("Property cannot be deleted because it has long-term requests");
        }
        favoriteRepository.deleteByProperty_Id(id);
        availabilityBlockRepository.deleteAllByPropertyId(id);
        propertyRepository.delete(property);
    }

    @Override
    @Transactional
    public PropertyPhotoDto uploadPhoto(Long propertyId, MultipartFile file) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        User currentUser = authenticationService.getCurrentUser();
        assertCanManageProperty(property, currentUser);
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
    public void deletePhoto(Long propertyId, Long photoId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        User currentUser = authenticationService.getCurrentUser();
        assertCanManageProperty(property, currentUser);

        PropertyPhoto photo = propertyPhotoRepository.findByIdAndPropertyId(photoId, propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found for the property"));
        propertyPhotoRepository.delete(photo);
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

    private void updateAmenities(Property property, List<Long> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) {
            property.setAmenities(new HashSet<>());
            return;
        }
        List<Amenity> foundAmenities = amenityRepository.findAllById(amenityIds);
        if (foundAmenities.size() != amenityIds.size()) {
            throw new EntityNotFoundException("One or more amenities provided in the request do not exist.");
        }
        property.setAmenities(new HashSet<>(foundAmenities));
    }

    private void updateAddress(Property property, PropertyCreateRequestDto request) {
        if (request.address() == null) {
            return;
        }
        if (property.getAddress() == null) {
            Address newAddress = propertyMapper.toAddressEntity(request.address());
            if (newAddress.getLocation() != null) {
                Location savedLocation = locationRepository.save(newAddress.getLocation());
                newAddress.setLocation(savedLocation);
            }
            Address savedAddress = addressRepository.save(newAddress);
            property.setAddress(savedAddress);
            return;
        }

        Address existingAddress = property.getAddress();
        if (existingAddress.getLocation() == null) {
            existingAddress.setLocation(new Location());
        }
        Location location = existingAddress.getLocation();
        location.setCountry(request.address().location().country());
        location.setRegion(request.address().location().region());
        location.setCity(request.address().location().city());
        locationRepository.save(location);

        existingAddress.setStreet(request.address().street());
        existingAddress.setHouseNumber(request.address().houseNumber());
        existingAddress.setApartment(request.address().apartment());
        existingAddress.setPostalCode(request.address().postalCode());
        existingAddress.setLat(request.address().lat());
        existingAddress.setLng(request.address().lng());
        addressRepository.save(existingAddress);
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
        property.getPricing().setPricePerNight(request.pricing().pricePerNight());
        property.getPricing().setPricePerMonth(request.pricing().pricePerMonth());
        property.getPricing().setCurrency(request.pricing().currency());
        property.getPricing().setSecurityDeposit(request.pricing().securityDeposit());
        property.getPricing().setCleaningFee(request.pricing().cleaningFee());
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
        property.getRules().setPetsAllowed(request.rules().petsAllowed());
        property.getRules().setSmokingAllowed(request.rules().smokingAllowed());
        property.getRules().setPartiesAllowed(request.rules().partiesAllowed());
        property.getRules().setAdditionalRules(request.rules().additionalRules());
    }

    private void assertCanManageProperty(Property property, User currentUser) {
        boolean isHost = property.getHost().getId().equals(currentUser.getId());
        boolean isAdmin = isAdmin(currentUser);
        if (!isHost && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to manage this property");
        }
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
    }
}
