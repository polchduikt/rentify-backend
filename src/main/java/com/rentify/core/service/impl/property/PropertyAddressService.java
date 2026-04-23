package com.rentify.core.service.impl.property;

import com.rentify.core.dto.property.AddressDto;
import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.entity.Address;
import com.rentify.core.entity.City;
import com.rentify.core.entity.District;
import com.rentify.core.entity.MetroStation;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.ResidentialComplex;
import com.rentify.core.mapper.PropertyMapper;
import com.rentify.core.repository.AddressRepository;
import com.rentify.core.repository.CityRepository;
import com.rentify.core.repository.DistrictRepository;
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

        if (cityRef == null) {
            throw DomainException.badRequest("ADDRESS_CITY_REQUIRED", "Either cityId or city name must be provided");
        }

        if (districtRef != null && !districtRef.getCity().getId().equals(cityRef.getId())) {
            throw DomainException.badRequest("ADDRESS_REFERENCE_CONFLICT", "District does not belong to selected city");
        }
        if (metroStationRef != null && !metroStationRef.getCity().getId().equals(cityRef.getId())) {
            throw DomainException.badRequest("ADDRESS_REFERENCE_CONFLICT", "Metro station does not belong to selected city");
        }
        if (residentialComplexRef != null && !residentialComplexRef.getCity().getId().equals(cityRef.getId())) {
            throw DomainException.badRequest("ADDRESS_REFERENCE_CONFLICT", "Residential complex does not belong to selected city");
        }

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

    private City resolveCityReference(AddressDto addressDto) {
        if (addressDto.cityId() != null) {
            return cityRepository.findById(addressDto.cityId())
                    .orElseThrow(() -> new EntityNotFoundException("City not found"));
        }
        if (addressDto.city() != null
                && !addressDto.city().isBlank()
                && addressDto.region() != null
                && !addressDto.region().isBlank()) {
            City found = cityRepository.findFirstByNameIgnoreCaseAndRegionIgnoreCase(
                    addressDto.city(),
                    addressDto.region()
            ).orElse(null);
            if (found != null) {
                return found;
            }
        }
        if (addressDto.city() != null && !addressDto.city().isBlank()
                && addressDto.country() != null && !addressDto.country().isBlank()) {
            return cityRepository.findFirstByNameIgnoreCaseAndCountryIgnoreCase(
                    addressDto.city(),
                    addressDto.country()
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
