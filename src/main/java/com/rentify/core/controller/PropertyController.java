package com.rentify.core.controller;

import com.rentify.core.dto.PropertyCreateRequestDto;
import com.rentify.core.dto.PropertyResponseDto;
import com.rentify.core.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

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
}