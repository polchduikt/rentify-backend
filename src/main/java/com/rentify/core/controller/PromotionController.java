package com.rentify.core.controller;

import com.rentify.core.dto.promotion.PurchaseSubscriptionRequestDto;
import com.rentify.core.dto.promotion.PurchaseTopPromotionRequestDto;
import com.rentify.core.dto.promotion.SubscriptionPackageDto;
import com.rentify.core.dto.promotion.SubscriptionPurchaseResponseDto;
import com.rentify.core.dto.promotion.TopPromotionPackageDto;
import com.rentify.core.dto.promotion.TopPromotionPurchaseResponseDto;
import com.rentify.core.service.PromotionService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotions", description = "Top promotion and subscription purchase endpoints")
@SecurityRequirement(name = "bearerAuth")
@Validated
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping("/top-packages")
    @Operation(
            summary = "Get top promotion packages",
            description = "Returns available top-promotion package options with duration and price."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Top promotion packages retrieved",
            content = @Content(schema = @Schema(implementation = TopPromotionPackageDto.class))
    )
    public ResponseEntity<List<TopPromotionPackageDto>> getTopPromotionPackages() {
        return ResponseEntity.ok(promotionService.getTopPromotionPackages());
    }

    @GetMapping("/subscription-packages")
    @Operation(
            summary = "Get subscription packages",
            description = "Returns available subscription package options for host account upgrades."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Subscription packages retrieved",
            content = @Content(schema = @Schema(implementation = SubscriptionPackageDto.class))
    )
    public ResponseEntity<List<SubscriptionPackageDto>> getSubscriptionPackages() {
        return ResponseEntity.ok(promotionService.getSubscriptionPackages());
    }

    @PostMapping("/properties/{propertyId}/top")
    @Operation(
            summary = "Purchase top promotion for property",
            description = "Purchases selected top-promotion package and applies promotion to chosen property."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Top promotion purchased",
                    content = @Content(schema = @Schema(implementation = TopPromotionPurchaseResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Property not found")
    })
    public ResponseEntity<TopPromotionPurchaseResponseDto> purchaseTopPromotion(
            @Parameter(description = "Property ID", example = "42")
            @PathVariable @Positive Long propertyId,
            @Valid @RequestBody PurchaseTopPromotionRequestDto request) {
        return ResponseEntity.ok(promotionService.purchaseTopPromotion(propertyId, request.packageType()));
    }

    @PostMapping("/subscription")
    @Operation(
            summary = "Purchase subscription",
            description = "Purchases selected subscription package and updates current user subscription plan."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Subscription purchased",
            content = @Content(schema = @Schema(implementation = SubscriptionPurchaseResponseDto.class))
    )
    public ResponseEntity<SubscriptionPurchaseResponseDto> purchaseSubscription(
            @Valid @RequestBody PurchaseSubscriptionRequestDto request) {
        return ResponseEntity.ok(promotionService.purchaseSubscription(request.packageType()));
    }
}
