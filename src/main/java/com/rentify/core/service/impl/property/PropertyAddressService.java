package com.rentify.core.service.impl.property;

import com.rentify.core.dto.property.AddressDto;
import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.entity.Address;
import com.rentify.core.entity.City;
import com.rentify.core.entity.District;
import com.rentify.core.entity.Location;
import com.rentify.core.entity.MetroStation;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.ResidentialComplex;
import com.rentify.core.mapper.PropertyMapper;
import com.rentify.core.repository.AddressRepository;
import com.rentify.core.repository.CityRepository;
import com.rentify.core.repository.DistrictRepository;
import com.rentify.core.repository.LocationRepository;
import com.rentify.core.repository.MetroStationRepository;
import com.rentify.core.repository.ResidentialComplexRepository;
import com.rentify.core.exception.DomainException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PropertyAddressService {

    private final AddressRepository addressRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final MetroStationRepository metroStationRepository;
    private final ResidentialComplexRepository residentialComplexRepository;
    private final LocationRepository locationRepository;
    private final PropertyMapper propertyMapper;

    public void updateAddress(Property property, PropertyCreateRequestDto request) {
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
                throw DomainException.badRequest("ADDRESS_REFERENCE_CONFLICT", "District does not belong to selected city");
            }
            if (metroStationRef != null && !metroStationRef.getCity().getId().equals(cityRef.getId())) {
                throw DomainException.badRequest("ADDRESS_REFERENCE_CONFLICT", "Metro station does not belong to selected city");
            }
            if (residentialComplexRef != null && !residentialComplexRef.getCity().getId().equals(cityRef.getId())) {
                throw DomainException.badRequest("ADDRESS_REFERENCE_CONFLICT", "Residential complex does not belong to selected city");
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
            throw DomainException.badRequest("ADDRESS_LOCATION_REQUIRED", "Either cityId or address.location must be provided");
        }
        Location savedLocation = resolveOrCreateLocation(location);

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

    private Location resolveOrCreateLocation(Location location) {
        String country = normalizeRequiredLocationValue(location.getCountry(), "country");
        String region = normalizeRegionValue(location.getRegion());
        String city = normalizeRequiredLocationValue(location.getCity(), "city");
        location.setCountry(country);
        location.setRegion(region);
        location.setCity(city);
        return locationRepository.findByCityAndRegionAndCountry(city, region, country)
                .orElseGet(() -> locationRepository.save(location));
    }

    private String normalizeRequiredLocationValue(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw DomainException.badRequest("ADDRESS_LOCATION_REQUIRED", "Location " + fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String normalizeRegionValue(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
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
}
