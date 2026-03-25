package com.rentify.core.service.impl;

import com.rentify.core.dto.property.AmenityCategoryGroupDto;
import com.rentify.core.dto.property.AmenityDto;
import com.rentify.core.entity.Amenity;
import com.rentify.core.enums.AmenityCategory;
import com.rentify.core.mapper.AmenityMapper;
import com.rentify.core.repository.AmenityRepository;
import com.rentify.core.service.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AmenityServiceImpl implements AmenityService {

    private final AmenityRepository amenityRepository;
    private final AmenityMapper amenityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AmenityDto> getAmenities(AmenityCategory category) {
        List<Amenity> amenities = category == null
                ? amenityRepository.findAllByOrderByCategoryAscNameAsc()
                : amenityRepository.findAllByCategoryOrderByNameAsc(category);
        return amenityMapper.toDtos(amenities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmenityCategoryGroupDto> getAmenitiesGrouped() {
        List<Amenity> amenities = amenityRepository.findAllByOrderByCategoryAscNameAsc();
        Map<AmenityCategory, List<AmenityDto>> grouped = new LinkedHashMap<>();
        for (AmenityCategory value : AmenityCategory.values()) {
            grouped.put(value, new ArrayList<>());
        }

        for (Amenity amenity : amenities) {
            AmenityCategory category = amenity.getCategory() != null
                    ? amenity.getCategory()
                    : AmenityCategory.OTHER;
            grouped.computeIfAbsent(category, key -> new ArrayList<>()).add(amenityMapper.toDto(amenity));
        }

        return grouped.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> amenityMapper.toCategoryGroupDto(entry.getKey(), entry.getValue()))
                .toList();
    }
}
