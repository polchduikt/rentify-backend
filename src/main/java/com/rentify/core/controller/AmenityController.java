package com.rentify.core.controller;

import com.rentify.core.dto.property.AmenityCategoryGroupDto;
import com.rentify.core.dto.property.AmenityDto;
import com.rentify.core.enums.AmenityCategory;
import com.rentify.core.service.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/amenities")
@RequiredArgsConstructor
public class AmenityController {

    private final AmenityService amenityService;

    @GetMapping
    public ResponseEntity<List<AmenityDto>> getAmenities(
            @RequestParam(value = "category", required = false) AmenityCategory category) {
        return ResponseEntity.ok(amenityService.getAmenities(category));
    }

    @GetMapping("/grouped")
    public ResponseEntity<List<AmenityCategoryGroupDto>> getAmenitiesGrouped() {
        return ResponseEntity.ok(amenityService.getAmenitiesGrouped());
    }
}
