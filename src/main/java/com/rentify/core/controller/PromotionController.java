package com.rentify.core.controller;

import com.rentify.core.dto.promotion.SubscriptionPackageDto;
import com.rentify.core.dto.promotion.TopPromotionPackageDto;
import com.rentify.core.service.PromotionService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotion-packages")
@RequiredArgsConstructor
@Tag(name = "Promotion Packages", description = "Top promotion and subscription package read endpoints")
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

    @GetMapping("/top")
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

    @GetMapping("/subscriptions")
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

}
