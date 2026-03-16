package com.rentify.core.service;

import com.rentify.core.dto.location.LocationSuggestionDto;

import java.util.List;

public interface LocationDirectoryService {
    List<LocationSuggestionDto> suggest(String query, Long cityId, List<String> types, Integer limit);
}
