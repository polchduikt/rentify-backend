package com.rentify.core.service;

import com.rentify.core.dto.promotion.SubscriptionPackageDto;
import com.rentify.core.dto.promotion.SubscriptionPurchaseResponseDto;
import com.rentify.core.dto.promotion.TopPromotionPackageDto;
import com.rentify.core.dto.promotion.TopPromotionPurchaseResponseDto;
import com.rentify.core.enums.SubscriptionPackageType;
import com.rentify.core.enums.TopPromotionPackageType;

import java.util.List;

public interface PromotionService {
    TopPromotionPurchaseResponseDto purchaseTopPromotion(Long propertyId, TopPromotionPackageType packageType);
    SubscriptionPurchaseResponseDto purchaseSubscription(SubscriptionPackageType packageType);
    List<TopPromotionPackageDto> getTopPromotionPackages();
    List<SubscriptionPackageDto> getSubscriptionPackages();
}
