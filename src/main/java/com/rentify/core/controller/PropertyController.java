package com.rentify.core.controller;

import com.rentify.core.dto.property.PropertyCreateRequestDto;
import com.rentify.core.dto.property.PropertyMapPinDto;
import com.rentify.core.dto.property.PropertyResponseDto;
import com.rentify.core.dto.property.PropertySearchCriteriaDto;
import com.rentify.core.dto.property.PropertyStatusUpdateRequestDto;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Validated
@Tag(name = "Properties", description = "Property listing and search endpoints")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping
    @Operation(
            summary = "Get properties",
            description = "Returns paginated public property listings with optional filters from query parameters."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Properties retrieved",
            content = @Content(schema = @Schema(implementation = PropertyResponseDto.class))
    )
    public ResponseEntity<Page<PropertyResponseDto>> getProperties(
            @ParameterObject PropertySearchCriteriaDto criteria,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(propertyService.search(criteria, pageable));
    }

    @GetMapping("/me")
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
            @PathVariable @Positive Long id) {
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
            @PathVariable @Positive Long id,
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
            @PathVariable @Positive Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/map-pins")
    @Operation(
            summary = "Search map pins",
            description = "Returns map pin data for properties using the same filters as /properties."
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

    @PatchMapping("/{id}")
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
            @PathVariable @Positive Long id,
            @Valid @RequestBody PropertyStatusUpdateRequestDto request) {
        return ResponseEntity.ok(propertyService.changePropertyStatus(id, request.status()));
    }
}
