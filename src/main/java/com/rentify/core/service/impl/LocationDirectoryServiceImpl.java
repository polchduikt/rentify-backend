package com.rentify.core.service.impl;

import com.rentify.core.dto.location.LocationSuggestionDto;
import com.rentify.core.enums.LocationSuggestionType;
import com.rentify.core.mapper.LocationMapper;
import com.rentify.core.repository.CityRepository;
import com.rentify.core.repository.DistrictRepository;
import com.rentify.core.repository.MetroStationRepository;
import com.rentify.core.repository.ResidentialComplexRepository;
import com.rentify.core.service.LocationDirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LocationDirectoryServiceImpl implements LocationDirectoryService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final MetroStationRepository metroStationRepository;
    private final ResidentialComplexRepository residentialComplexRepository;
    private final LocationMapper locationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<LocationSuggestionDto> suggest(String query, Long cityId, List<String> types, Integer limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String normalizedQuery = escapeLikePattern(query.trim().toLowerCase(Locale.ROOT));
        int finalLimit = resolveLimit(limit);

        EnumSet<LocationSuggestionType> requestedTypes = resolveTypes(types);
        int perTypeLimit = Math.max(1, finalLimit / requestedTypes.size());

        List<LocationSuggestionDto> result = new ArrayList<>();
        if (requestedTypes.contains(LocationSuggestionType.CITY)) {
            result.addAll(locationMapper.toCitySuggestions(
                    cityRepository.searchByPrefix(normalizedQuery, PageRequest.of(0, perTypeLimit))
            ));
        }
        if (requestedTypes.contains(LocationSuggestionType.DISTRICT)) {
            result.addAll(locationMapper.toDistrictSuggestions(
                    districtRepository.searchByPrefix(normalizedQuery, cityId, PageRequest.of(0, perTypeLimit))
            ));
        }
        if (requestedTypes.contains(LocationSuggestionType.METRO)) {
            result.addAll(locationMapper.toMetroSuggestions(
                    metroStationRepository.searchByPrefix(normalizedQuery, cityId, PageRequest.of(0, perTypeLimit))
            ));
        }
        if (requestedTypes.contains(LocationSuggestionType.RESIDENTIAL_COMPLEX)) {
            result.addAll(locationMapper.toResidentialComplexSuggestions(
                    residentialComplexRepository.searchByPrefix(normalizedQuery, cityId, PageRequest.of(0, perTypeLimit))
            ));
        }

        if (result.size() > finalLimit) {
            return result.subList(0, finalLimit);
        }
        return result;
    }

    private EnumSet<LocationSuggestionType> resolveTypes(List<String> types) {
        if (types == null || types.isEmpty()) {
            return EnumSet.allOf(LocationSuggestionType.class);
        }
        EnumSet<LocationSuggestionType> resolved = EnumSet.noneOf(LocationSuggestionType.class);
        for (String rawType : types) {
            if (rawType == null || rawType.isBlank()) {
                continue;
            }
            try {
                resolved.add(LocationSuggestionType.valueOf(rawType.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // Skip unknown values to keep endpoint resilient to client typos.
            }
        }
        if (resolved.isEmpty()) {
            return EnumSet.allOf(LocationSuggestionType.class);
        }
        return resolved;
    }

    private int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String escapeLikePattern(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
