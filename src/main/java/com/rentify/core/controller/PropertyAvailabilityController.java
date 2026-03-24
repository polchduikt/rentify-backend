package com.rentify.core.controller;

import com.rentify.core.dto.property.AvailabilityBlockDto;
import com.rentify.core.dto.property.AvailabilityBlockRequestDto;
import com.rentify.core.dto.property.UnavailableDateRangeDto;
import com.rentify.core.service.AvailabilityService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/properties/{propertyId}")
@RequiredArgsConstructor
@Validated
@Tag(name = "Property Availability", description = "Property availability blocks and unavailable ranges")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class PropertyAvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping("/availability-blocks")
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
            @PathVariable @Positive Long propertyId,
            @Valid @RequestBody AvailabilityBlockRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(availabilityService.createBlock(propertyId, request));
    }

    @GetMapping("/availability-blocks")
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
            @PathVariable @Positive Long propertyId) {
        return ResponseEntity.ok(availabilityService.getBlocksByProperty(propertyId));
    }

    @GetMapping("/unavailable-date-ranges")
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
            @PathVariable @Positive Long propertyId,
            @Parameter(description = "Start date filter (ISO yyyy-MM-dd)", example = "2026-03-20")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "End date filter (ISO yyyy-MM-dd)", example = "2026-03-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(availabilityService.getUnavailableRangesByProperty(propertyId, dateFrom, dateTo));
    }

    @DeleteMapping("/availability-blocks/{blockId}")
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
            @PathVariable @Positive Long propertyId,
            @Parameter(description = "Availability block ID", example = "77")
            @PathVariable @Positive Long blockId) {
        availabilityService.deleteBlock(propertyId, blockId);
        return ResponseEntity.noContent().build();
    }
}
