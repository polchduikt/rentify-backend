package com.rentify.core.controller;

import com.rentify.core.dto.promotion.PurchaseTopPromotionRequestDto;
import com.rentify.core.dto.promotion.TopPromotionPurchaseResponseDto;
import com.rentify.core.service.PromotionService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Tag(name = "Property Promotions", description = "Property promotion purchase endpoints")
@SecurityRequirement(name = "bearerAuth")
@Validated
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class PropertyPromotionController {

    private final PromotionService promotionService;

    @PostMapping("/{propertyId}/promotions")
    @Operation(
            summary = "Purchase promotion for property",
            description = "Purchases selected top-promotion package and applies promotion to chosen property."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Promotion purchased",
                    content = @Content(schema = @Schema(implementation = TopPromotionPurchaseResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Property not found")
    })
    public ResponseEntity<TopPromotionPurchaseResponseDto> purchasePromotion(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable @Positive Long propertyId,
            @Valid @RequestBody PurchaseTopPromotionRequestDto request) {
        return ResponseEntity.ok(promotionService.purchaseTopPromotion(propertyId, request.packageType()));
    }
}
