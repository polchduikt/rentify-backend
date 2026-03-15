package com.rentify.core.controller;

import com.rentify.core.dto.property.AvailabilityBlockDto;
import com.rentify.core.dto.property.AvailabilityBlockRequestDto;
import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.dto.property.PropertyMapPinDto;
import com.rentify.core.dto.property.PropertyPhotoDto;
import com.rentify.core.dto.property.PropertyResponseDto;
import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import com.rentify.core.dto.property.PropertyStatusUpdateRequestDto;
import com.rentify.core.dto.property.UnavailableDateRangeDto;
import com.rentify.core.enums.PropertyStatus;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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

    @GetMapping("/my")
    public ResponseEntity<Page<PropertyResponseDto>> getMyProperties(
            @RequestParam(required = false) List<PropertyStatus> statuses,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(propertyService.getCurrentUserProperties(pageable, statuses));
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

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponseDto> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyCreateRequestDto request) {
        return ResponseEntity.ok(propertyService.updateProperty(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PropertyPhotoDto> uploadPhoto(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        PropertyPhotoDto uploadedPhoto = propertyService.uploadPhoto(id, file);
        return ResponseEntity.ok(uploadedPhoto);
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id, @PathVariable Long photoId) {
        propertyService.deletePhoto(id, photoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PropertyResponseDto>> searchProperties(
            @ParameterObject PropertySearchCriteriaDto criteria,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(propertyService.search(criteria, pageable));
    }

    @GetMapping("/search/map-pins")
    public ResponseEntity<Page<PropertyMapPinDto>> searchMapPins(
            @ParameterObject PropertySearchCriteriaDto criteria,
            @PageableDefault(size = 200, sort = "createdAt", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(propertyService.searchMapPins(criteria, pageable));
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

    @GetMapping("/{propertyId}/availability/unavailable")
    public ResponseEntity<List<UnavailableDateRangeDto>> getUnavailableRanges(
            @PathVariable Long propertyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(availabilityService.getUnavailableRangesByProperty(propertyId, dateFrom, dateTo));
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
