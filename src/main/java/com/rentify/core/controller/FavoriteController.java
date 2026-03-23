package com.rentify.core.controller;

import com.rentify.core.dto.favorite.FavoriteResponseDto;
import com.rentify.core.service.FavoriteService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Current user favorite properties")
@SecurityRequirement(name = "bearerAuth")
@Validated
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PutMapping("/{propertyId}")
    @Operation(
            summary = "Add property to favorites",
            description = "Adds property to authenticated user favorites list if it is not already in favorites."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Property added to favorites",
                    content = @Content(schema = @Schema(implementation = FavoriteResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Property not found")
    })
    public ResponseEntity<FavoriteResponseDto> addToFavorites(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable @Positive Long propertyId) {
        return ResponseEntity.ok(favoriteService.addToFavorites(propertyId));
    }

    @DeleteMapping("/{propertyId}")
    @Operation(
            summary = "Remove property from favorites",
            description = "Removes property from authenticated user favorites list."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Property removed from favorites"),
            @ApiResponse(responseCode = "404", description = "Property or favorite entry not found")
    })
    public ResponseEntity<Void> removeFromFavorites(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable @Positive Long propertyId) {
        favoriteService.removeFromFavorites(propertyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "Get current user favorites",
            description = "Returns all favorite properties of the authenticated user."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Favorites retrieved",
            content = @Content(schema = @Schema(implementation = FavoriteResponseDto.class))
    )
    public ResponseEntity<List<FavoriteResponseDto>> getMyFavorites() {
        return ResponseEntity.ok(favoriteService.getMyFavorites());
    }
}
