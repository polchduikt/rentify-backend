package com.rentify.core.service.impl;

import com.rentify.core.dto.property.AddressDto;
import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.dto.property.PropertyPhotoDto;
import com.rentify.core.dto.property.PropertyResponseDto;
import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import com.rentify.core.entity.Address;
import com.rentify.core.entity.Amenity;
import com.rentify.core.entity.City;
import com.rentify.core.entity.District;
import com.rentify.core.entity.Location;
import com.rentify.core.entity.MetroStation;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.PropertyPhoto;
import com.rentify.core.entity.PropertyPricing;
import com.rentify.core.entity.PropertyRule;
import com.rentify.core.entity.ResidentialComplex;
import com.rentify.core.entity.User;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.mapper.PropertyMapper;
import com.rentify.core.repository.AddressRepository;
import com.rentify.core.repository.AmenityRepository;
import com.rentify.core.repository.AvailabilityBlockRepository;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.CityRepository;
import com.rentify.core.repository.ConversationRepository;
import com.rentify.core.repository.DistrictRepository;
import com.rentify.core.repository.FavoriteRepository;
import com.rentify.core.repository.LocationRepository;
import com.rentify.core.repository.MetroStationRepository;
import com.rentify.core.repository.PropertyPhotoRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.repository.ResidentialComplexRepository;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final AmenityRepository amenityRepository;
    private final PropertyMapper propertyMapper;
    private final AddressRepository addressRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final MetroStationRepository metroStationRepository;
    private final ResidentialComplexRepository residentialComplexRepository;
    private final LocationRepository locationRepository;
    private final AuthenticationService authenticationService;
    private final CloudinaryService cloudinaryService;
    private final PropertyPhotoRepository propertyPhotoRepository;
    private final AvailabilityBlockRepository availabilityBlockRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final ConversationRepository conversationRepository;
    private final FavoriteRepository favoriteRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponseDto> getAllProperties(Pageable pageable) {
        Page<Property> propertiesPage = propertyRepository.findAll(pageable);
        return propertiesPage.map(propertyMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyResponseDto> getCurrentUserProperties(Pageable pageable) {
        User currentUser = authenticationService.getCurrentUser();
        return propertyRepository.findAllByHostId(currentUser.getId(), pageable)
                .map(propertyMapper::toDto);
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
        property.setMarketType(request.marketType());
        applyListingFlags(property, request);
        property.setHost(host);
        property.setStatus(PropertyStatus.ACTIVE);
        if (property.getPricing() != null) {
            property.getPricing().setProperty(property);
        }
        if (property.getRules() != null) {
            property.getRules().setProperty(property);
        }
        updateAmenities(property, request.amenityIds(), request.amenitySlugs());
        updateAddress(property, request);
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
        property.setMarketType(request.marketType());
        applyListingFlags(property, request);
        property.setRooms(request.rooms());
        property.setFloor(request.floor());
        property.setTotalFloors(request.totalFloors());
        property.setAreaSqm(request.areaSqm());
        property.setMaxGuests(request.maxGuests());
        property.setCheckInTime(request.checkInTime());
        property.setCheckOutTime(request.checkOutTime());
        updateAmenities(property, request.amenityIds(), request.amenitySlugs());
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
        validateSearchDateRange(criteria);
        return propertyRepository.findAll(PropertySpecifications.withFilters(criteria), pageable)
                .map(propertyMapper::toDto);
    }

    private void updateAmenities(Property property, List<Long> amenityIds, List<String> amenitySlugs) {
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

        property.setAmenities(amenities);
    }

    private void updateAddress(Property property, PropertyCreateRequestDto request) {
        if (request.address() == null) {
            return;
        }
        AddressDto dto = request.address();
        Address address = property.getAddress();
        if (address == null) {
            address = propertyMapper.toAddressEntity(dto);
        }

        City cityRef = resolveCityReference(dto);
        District districtRef = resolveDistrictReference(dto);
        MetroStation metroStationRef = resolveMetroStationReference(dto);
        ResidentialComplex residentialComplexRef = resolveResidentialComplexReference(dto);

        cityRef = resolveCityFromReferences(cityRef, districtRef, metroStationRef, residentialComplexRef);

        if (cityRef != null) {
            if (districtRef != null && !districtRef.getCity().getId().equals(cityRef.getId())) {
                throw new IllegalArgumentException("District does not belong to selected city");
            }
            if (metroStationRef != null && !metroStationRef.getCity().getId().equals(cityRef.getId())) {
                throw new IllegalArgumentException("Metro station does not belong to selected city");
            }
            if (residentialComplexRef != null && !residentialComplexRef.getCity().getId().equals(cityRef.getId())) {
                throw new IllegalArgumentException("Residential complex does not belong to selected city");
            }
        }

        if (address.getLocation() == null) {
            address.setLocation(new Location());
        }
        Location location = address.getLocation();
        if (cityRef != null) {
            location.setCountry(cityRef.getCountry());
            location.setRegion(cityRef.getRegion());
            location.setCity(cityRef.getName());
        } else if (dto.location() != null) {
            location.setCountry(dto.location().country());
            location.setRegion(dto.location().region());
            location.setCity(dto.location().city());
        } else {
            throw new IllegalArgumentException("Either cityId or address.location must be provided");
        }
        Location savedLocation = locationRepository.save(location);

        address.setLocation(savedLocation);
        address.setCityRef(cityRef);
        address.setDistrictRef(districtRef);
        address.setMetroStationRef(metroStationRef);
        address.setResidentialComplexRef(residentialComplexRef);
        address.setStreet(dto.street());
        address.setHouseNumber(dto.houseNumber());
        address.setApartment(dto.apartment());
        address.setPostalCode(dto.postalCode());
        address.setLat(dto.lat());
        address.setLng(dto.lng());

        Address savedAddress = addressRepository.save(address);
        property.setAddress(savedAddress);
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

    private void applyListingFlags(Property property, PropertyCreateRequestDto request) {
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

    private City resolveCityReference(AddressDto addressDto) {
        if (addressDto.cityId() != null) {
            return cityRepository.findById(addressDto.cityId())
                    .orElseThrow(() -> new EntityNotFoundException("City not found"));
        }
        if (addressDto.location() != null
                && addressDto.location().city() != null
                && !addressDto.location().city().isBlank()
                && addressDto.location().region() != null
                && !addressDto.location().region().isBlank()) {
            return cityRepository.findFirstByNameIgnoreCaseAndRegionIgnoreCase(
                    addressDto.location().city(),
                    addressDto.location().region()
            ).orElse(null);
        }
        return null;
    }

    private District resolveDistrictReference(AddressDto addressDto) {
        if (addressDto.districtId() == null) {
            return null;
        }
        return districtRepository.findById(addressDto.districtId())
                .orElseThrow(() -> new EntityNotFoundException("District not found"));
    }

    private MetroStation resolveMetroStationReference(AddressDto addressDto) {
        if (addressDto.metroStationId() == null) {
            return null;
        }
        return metroStationRepository.findById(addressDto.metroStationId())
                .orElseThrow(() -> new EntityNotFoundException("Metro station not found"));
    }

    private ResidentialComplex resolveResidentialComplexReference(AddressDto addressDto) {
        if (addressDto.residentialComplexId() == null) {
            return null;
        }
        return residentialComplexRepository.findById(addressDto.residentialComplexId())
                .orElseThrow(() -> new EntityNotFoundException("Residential complex not found"));
    }

    private City resolveCityFromReferences(
            City initialCity,
            District district,
            MetroStation metroStation,
            ResidentialComplex residentialComplex
    ) {
        if (initialCity != null) {
            return initialCity;
        }
        if (district != null) {
            return district.getCity();
        }
        if (metroStation != null) {
            return metroStation.getCity();
        }
        if (residentialComplex != null) {
            return residentialComplex.getCity();
        }
        return null;
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

    private void validateSearchDateRange(PropertySearchCriteriaDto criteria) {
        if ((criteria.dateFrom() == null) != (criteria.dateTo() == null)) {
            throw new IllegalArgumentException("Both dateFrom and dateTo must be provided together for availability search.");
        }
        if (criteria.dateFrom() != null && !criteria.dateFrom().isBefore(criteria.dateTo())) {
            throw new IllegalArgumentException("dateFrom must be before dateTo for availability search.");
        }
    }
}
