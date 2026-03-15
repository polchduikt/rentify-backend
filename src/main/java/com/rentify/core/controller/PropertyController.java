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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Properties", description = "Property listing, search, photos and availability endpoints")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class PropertyController {

    private final PropertyService propertyService;
    private final AvailabilityService availabilityService;

    @GetMapping
    @Operation(
            summary = "Get all properties",
            description = "Returns paginated public property listings sorted by newest first by default."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Properties retrieved",
            content = @Content(schema = @Schema(implementation = PropertyResponseDto.class))
    )
    public ResponseEntity<Page<PropertyResponseDto>> getAllProperties(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(propertyService.getAllProperties(pageable));
    }

    @GetMapping("/my")
    @Operation(
            summary = "Get current user properties",
            description = "Returns paginated properties owned by the current user. Optional statuses filter supports repeated values."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(
            responseCode = "200",
            description = "Current user properties retrieved",
            content = @Content(schema = @Schema(implementation = PropertyResponseDto.class))
    )
    public ResponseEntity<Page<PropertyResponseDto>> getMyProperties(
            @Parameter(description = "Optional property statuses. Repeat parameter: statuses=ACTIVE&statuses=DRAFT",
                    example = "ACTIVE")
            @RequestParam(required = false) List<PropertyStatus> statuses,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(propertyService.getCurrentUserProperties(pageable, statuses));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get property by id",
            description = "Returns a single property by identifier and increments property view metrics."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Property found",
                    content = @Content(schema = @Schema(implementation = PropertyResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Property not found")
    })
    public ResponseEntity<PropertyResponseDto> getPropertyById(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }

    @PostMapping
    @Operation(
            summary = "Create property",
            description = "Creates a new property listing for the authenticated host in DRAFT/ACTIVE flow."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Property created",
                    content = @Content(schema = @Schema(implementation = PropertyResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PropertyResponseDto> createProperty(@Valid @RequestBody PropertyCreateRequestDto request) {
        PropertyResponseDto createdProperty = propertyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProperty);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update property",
            description = "Updates property data. Only property owner is allowed to update listing details."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Property updated",
                    content = @Content(schema = @Schema(implementation = PropertyResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Property not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PropertyResponseDto> updateProperty(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable Long id,
            @Valid @RequestBody PropertyCreateRequestDto request) {
        return ResponseEntity.ok(propertyService.updateProperty(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete property",
            description = "Deletes a property listing. Operation is available only to the listing owner."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Property deleted"),
            @ApiResponse(responseCode = "404", description = "Property not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> deleteProperty(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload property photo",
            description = "Uploads a photo file and attaches it to the property. Only owner can upload photos."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Photo uploaded",
                    content = @Content(schema = @Schema(implementation = PropertyPhotoDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Property not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PropertyPhotoDto> uploadPhoto(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable Long id,
            @Parameter(description = "Image file to upload")
            @RequestPart("file") MultipartFile file) {
        PropertyPhotoDto uploadedPhoto = propertyService.uploadPhoto(id, file);
        return ResponseEntity.ok(uploadedPhoto);
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    @Operation(
            summary = "Delete property photo",
            description = "Deletes a property photo by photo identifier. Only listing owner can delete photos."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Photo deleted"),
            @ApiResponse(responseCode = "404", description = "Property or photo not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> deletePhoto(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable Long id,
            @Parameter(description = "Photo ID", example = "101")
            @PathVariable Long photoId) {
        propertyService.deletePhoto(id, photoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search properties",
            description = "Searches properties by optional filters. All filters are query parameters."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Properties matched by filters",
            content = @Content(schema = @Schema(implementation = PropertyResponseDto.class))
    )
    public ResponseEntity<Page<PropertyResponseDto>> searchProperties(
            @ParameterObject PropertySearchCriteriaDto criteria,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(propertyService.search(criteria, pageable));
    }

    @GetMapping("/search/map-pins")
    @Operation(
            summary = "Search map pins",
            description = "Returns map pin data for properties using the same filters as /search."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Map pins retrieved",
            content = @Content(schema = @Schema(implementation = PropertyMapPinDto.class))
    )
    public ResponseEntity<Page<PropertyMapPinDto>> searchMapPins(
            @ParameterObject PropertySearchCriteriaDto criteria,
            @PageableDefault(size = 200, sort = "createdAt", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(propertyService.searchMapPins(criteria, pageable));
    }

    @PostMapping("/{id}/availability")
    @Operation(
            summary = "Create availability block",
            description = "Blocks a date range from booking for a property. Only owner can create blocks."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Availability block created",
                    content = @Content(schema = @Schema(implementation = AvailabilityBlockDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Property not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<AvailabilityBlockDto> createBlock(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityBlockRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(availabilityService.createBlock(id, request));
    }

    @GetMapping("/{propertyId}/availability")
    @Operation(
            summary = "Get availability blocks by property",
            description = "Returns manually created availability blocks for the selected property."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Availability blocks retrieved",
            content = @Content(schema = @Schema(implementation = AvailabilityBlockDto.class))
    )
    public ResponseEntity<List<AvailabilityBlockDto>> getBlocks(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable Long propertyId) {
        return ResponseEntity.ok(availabilityService.getBlocksByProperty(propertyId));
    }

    @GetMapping("/{propertyId}/availability/unavailable")
    @Operation(
            summary = "Get unavailable date ranges",
            description = "Returns merged unavailable ranges from both manual blocks and active bookings."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Unavailable date ranges retrieved",
            content = @Content(schema = @Schema(implementation = UnavailableDateRangeDto.class))
    )
    public ResponseEntity<List<UnavailableDateRangeDto>> getUnavailableRanges(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable Long propertyId,
            @Parameter(description = "Start date filter (ISO yyyy-MM-dd)", example = "2026-03-20")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "End date filter (ISO yyyy-MM-dd)", example = "2026-03-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(availabilityService.getUnavailableRangesByProperty(propertyId, dateFrom, dateTo));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Change property status",
            description = "Changes property status (for example DRAFT/ACTIVE/INACTIVE/BLOCKED) for the listing owner."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Property status changed",
                    content = @Content(schema = @Schema(implementation = PropertyResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Property not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PropertyResponseDto> changeStatus(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable Long id,
            @Valid @RequestBody PropertyStatusUpdateRequestDto request) {
        return ResponseEntity.ok(propertyService.changePropertyStatus(id, request.status()));
    }

    @DeleteMapping("/{propertyId}/availability/{blockId}")
    @Operation(
            summary = "Delete availability block",
            description = "Removes an existing manual availability block. Only listing owner can perform operation."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Availability block deleted"),
            @ApiResponse(responseCode = "404", description = "Property or block not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> deleteBlock(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable Long propertyId,
            @Parameter(description = "Availability block ID", example = "77")
            @PathVariable Long blockId) {
        availabilityService.deleteBlock(propertyId, blockId);
        return ResponseEntity.noContent().build();
    }
}
