package com.rentify.core.controller;

import com.rentify.core.dto.promotion.PurchaseSubscriptionRequestDto;
import com.rentify.core.dto.promotion.SubscriptionPurchaseResponseDto;
import com.rentify.core.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription purchase endpoints")
@SecurityRequirement(name = "bearerAuth")
@Validated
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class SubscriptionController {

    private final PromotionService promotionService;

    @PostMapping
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
