package com.rentify.core.controller;

import com.rentify.core.dto.property.AmenityCategoryGroupDto;
import com.rentify.core.dto.property.AmenityDto;
import com.rentify.core.enums.AmenityCategory;
import com.rentify.core.service.AmenityService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Amenities", description = "Amenity dictionary endpoints")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class AmenityController {

    private final AmenityService amenityService;

    @GetMapping
    @Operation(
            summary = "Get amenities",
            description = "Returns all available amenities or filters them by category."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Amenities retrieved",
            content = @Content(schema = @Schema(implementation = AmenityDto.class))
    )
    public ResponseEntity<List<AmenityDto>> getAmenities(
            @Parameter(description = "Amenity category filter", example = "BASIC")
            @RequestParam(value = "category", required = false) AmenityCategory category) {
        return ResponseEntity.ok(amenityService.getAmenities(category));
    }

    @GetMapping("/grouped")
    @Operation(
            summary = "Get amenities grouped by category",
            description = "Returns amenity dictionary grouped by category for property create/search filters."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Grouped amenities retrieved",
            content = @Content(schema = @Schema(implementation = AmenityCategoryGroupDto.class))
    )
    public ResponseEntity<List<AmenityCategoryGroupDto>> getAmenitiesGrouped() {
        return ResponseEntity.ok(amenityService.getAmenitiesGrouped());
    }
}
