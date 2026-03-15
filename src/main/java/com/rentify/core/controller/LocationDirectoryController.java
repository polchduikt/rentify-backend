package com.rentify.core.controller;

import com.rentify.core.dto.location.LocationSuggestionDto;
import com.rentify.core.service.LocationDirectoryService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Locations", description = "Location suggestion endpoints")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class LocationDirectoryController {

    private final LocationDirectoryService locationDirectoryService;

    @GetMapping("/suggest")
    @Operation(
            summary = "Suggest locations by query",
            description = "Returns location suggestions by text query with optional city and type constraints."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Location suggestions retrieved",
            content = @Content(schema = @Schema(implementation = LocationSuggestionDto.class))
    )
    public ResponseEntity<List<LocationSuggestionDto>> suggest(
            @Parameter(description = "Search text", example = "Kyiv")
            @RequestParam("q") @NotBlank @Size(min = 2, max = 100) String query,
            @Parameter(description = "Optional city id to narrow suggestions", example = "1")
            @RequestParam(value = "cityId", required = false) @Positive Long cityId,
            @Parameter(description = "Optional types filter. Repeat parameter or pass comma-separated values.",
                    example = "CITY")
            @RequestParam(value = "types", required = false) List<String> types,
            @Parameter(description = "Max number of results (1..50)", example = "10")
            @RequestParam(value = "limit", required = false) @Min(1) @Max(50) Integer limit
    ) {
        return ResponseEntity.ok(locationDirectoryService.suggest(query, cityId, types, limit));
    }
}
