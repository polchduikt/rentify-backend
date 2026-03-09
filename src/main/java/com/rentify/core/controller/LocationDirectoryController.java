package com.rentify.core.controller;

import com.rentify.core.dto.location.LocationSuggestionDto;
import com.rentify.core.service.LocationDirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationDirectoryController {

    private final LocationDirectoryService locationDirectoryService;

    @GetMapping("/suggest")
    public ResponseEntity<List<LocationSuggestionDto>> suggest(
            @RequestParam("q") String query,
            @RequestParam(value = "cityId", required = false) Long cityId,
            @RequestParam(value = "types", required = false) List<String> types,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return ResponseEntity.ok(locationDirectoryService.suggest(query, cityId, types, limit));
    }
}
