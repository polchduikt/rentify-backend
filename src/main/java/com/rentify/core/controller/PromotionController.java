package com.rentify.core.controller;

import com.rentify.core.dto.promotion.PurchaseSubscriptionRequestDto;
import com.rentify.core.dto.promotion.PurchaseTopPromotionRequestDto;
import com.rentify.core.dto.promotion.SubscriptionPackageDto;
import com.rentify.core.dto.promotion.SubscriptionPurchaseResponseDto;
import com.rentify.core.dto.promotion.TopPromotionPackageDto;
import com.rentify.core.dto.promotion.TopPromotionPurchaseResponseDto;
import com.rentify.core.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping("/top-packages")
    public ResponseEntity<List<TopPromotionPackageDto>> getTopPromotionPackages() {
        return ResponseEntity.ok(promotionService.getTopPromotionPackages());
    }

    @GetMapping("/subscription-packages")
    public ResponseEntity<List<SubscriptionPackageDto>> getSubscriptionPackages() {
        return ResponseEntity.ok(promotionService.getSubscriptionPackages());
    }

    @PostMapping("/properties/{propertyId}/top")
    public ResponseEntity<TopPromotionPurchaseResponseDto> purchaseTopPromotion(
            @PathVariable Long propertyId,
            @Valid @RequestBody PurchaseTopPromotionRequestDto request) {
        return ResponseEntity.ok(promotionService.purchaseTopPromotion(propertyId, request.packageType()));
    }

    @PostMapping("/subscription")
    public ResponseEntity<SubscriptionPurchaseResponseDto> purchaseSubscription(
            @Valid @RequestBody PurchaseSubscriptionRequestDto request) {
        return ResponseEntity.ok(promotionService.purchaseSubscription(request.packageType()));
    }
}
