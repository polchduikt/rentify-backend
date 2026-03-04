package com.rentify.core.controller;

import com.rentify.core.dto.property.AvailabilityBlockDto;
import com.rentify.core.dto.property.AvailabilityBlockRequestDto;
import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.dto.property.PropertyPhotoDto;
import com.rentify.core.dto.property.PropertyResponseDto;
import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import com.rentify.core.dto.property.PropertyStatusUpdateRequestDto;
import com.rentify.core.service.AvailabilityService;
import com.rentify.core.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(propertyService.getAllProperties(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponseDto> getPropertyById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }

    @PostMapping
    public ResponseEntity<PropertyResponseDto> createProperty(@Valid @RequestBody PropertyCreateRequestDto request) {
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

    @PostMapping("/{id}/availability")
    public ResponseEntity<AvailabilityBlockDto> createBlock(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityBlockRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(availabilityService.createBlock(id, request));
    }

    @GetMapping("/{propertyId}/availability")
    public ResponseEntity<List<AvailabilityBlockDto>> getBlocks(@PathVariable Long propertyId) {
        return ResponseEntity.ok(availabilityService.getBlocksByProperty(propertyId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PropertyResponseDto> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody PropertyStatusUpdateRequestDto request) {
        return ResponseEntity.ok(propertyService.changePropertyStatus(id, request.status()));
    }

    @DeleteMapping("/{propertyId}/availability/{blockId}")
    public ResponseEntity<Void> deleteBlock(
            @PathVariable Long propertyId,
            @PathVariable Long blockId) {
        availabilityService.deleteBlock(propertyId, blockId);
        return ResponseEntity.noContent().build();
    }
}
