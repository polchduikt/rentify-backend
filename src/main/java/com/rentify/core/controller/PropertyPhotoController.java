package com.rentify.core.controller;

import com.rentify.core.dto.property.PropertyPhotoDto;
import com.rentify.core.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/properties/{propertyId}/photos")
@RequiredArgsConstructor
@Validated
@Tag(name = "Property Photos", description = "Property photo management endpoints")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class PropertyPhotoController {

    private final PropertyService propertyService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload property photo",
            description = "Uploads a photo file and attaches it to the property. Only owner can upload photos."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Photo uploaded",
                    content = @Content(schema = @Schema(implementation = PropertyPhotoDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Property not found")
    })
    public ResponseEntity<PropertyPhotoDto> uploadPhoto(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable @Positive Long propertyId,
            @Parameter(description = "Image file to upload")
            @RequestPart("file") MultipartFile file) {
        PropertyPhotoDto uploadedPhoto = propertyService.uploadPhoto(propertyId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedPhoto);
    }

    @DeleteMapping("/{photoId}")
    @Operation(
            summary = "Delete property photo",
            description = "Deletes a property photo by photo identifier. Only listing owner can delete photos."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Photo deleted"),
            @ApiResponse(responseCode = "404", description = "Property or photo not found")
    })
    public ResponseEntity<Void> deletePhoto(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable @Positive Long propertyId,
            @Parameter(description = "Photo ID", example = "101")
            @PathVariable @Positive Long photoId) {
        propertyService.deletePhoto(propertyId, photoId);
        return ResponseEntity.noContent().build();
    }
}
