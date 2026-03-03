package com.rentify.core.controller;

import com.rentify.core.dto.*;
import com.rentify.core.service.AvailabilityService;
import com.rentify.core.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<Page<PropertyResponseDto>> getAllProperties(
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(propertyService.getAllProperties(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponseDto> getPropertyById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }

    @PostMapping
    public ResponseEntity<PropertyResponseDto> createProperty(@RequestBody PropertyCreateRequestDto request) {
        PropertyResponseDto createdProperty = propertyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProperty);
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PropertyPhotoDto> uploadPhoto(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        PropertyPhotoDto uploadedPhoto = propertyService.uploadPhoto(id, file);
        return ResponseEntity.ok(uploadedPhoto);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PropertyResponseDto>> searchProperties(
            @ParameterObject PropertySearchCriteriaDto criteria,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(propertyService.search(criteria, pageable));
    }

    @PostMapping
    public ResponseEntity<AvailabilityBlockDto> createBlock(
            @PathVariable Long propertyId,
            @Valid @RequestBody AvailabilityBlockRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(availabilityService.createBlock(propertyId, request));
    }

    @GetMapping
    public ResponseEntity<List<AvailabilityBlockDto>> getBlocks(@PathVariable Long propertyId) {
        return ResponseEntity.ok(availabilityService.getBlocksByProperty(propertyId));
    }

    @DeleteMapping("/{blockId}")
    public ResponseEntity<Void> deleteBlock(
            @PathVariable Long propertyId,
            @PathVariable Long blockId) {
        availabilityService.deleteBlock(propertyId, blockId);
        return ResponseEntity.noContent().build();
    }
}