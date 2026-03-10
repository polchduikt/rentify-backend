package com.rentify.core.controller;

import com.rentify.core.dto.location.LocationSuggestionDto;
import com.rentify.core.service.LocationDirectoryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@Validated
@RequiredArgsConstructor
public class LocationDirectoryController {

    private final LocationDirectoryService locationDirectoryService;

    @GetMapping("/suggest")
    public ResponseEntity<List<LocationSuggestionDto>> suggest(
            @RequestParam("q") @NotBlank @Size(min = 2, max = 100) String query,
            @RequestParam(value = "cityId", required = false) @Positive Long cityId,
            @RequestParam(value = "types", required = false) List<String> types,
            @RequestParam(value = "limit", required = false) @Min(1) @Max(50) Integer limit
    ) {
        return ResponseEntity.ok(locationDirectoryService.suggest(query, cityId, types, limit));
    }
}
