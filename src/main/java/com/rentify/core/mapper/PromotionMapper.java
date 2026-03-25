package com.rentify.core.mapper;

import com.rentify.core.dto.promotion.SubscriptionPackageDto;
import com.rentify.core.dto.promotion.SubscriptionPurchaseResponseDto;
import com.rentify.core.dto.promotion.TopPromotionPackageDto;
import com.rentify.core.dto.promotion.TopPromotionPurchaseResponseDto;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.enums.SubscriptionPackageType;
import com.rentify.core.enums.TopPromotionPackageType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(config = MapStructCentralConfig.class)
public interface PromotionMapper {

    @Mapping(source = "property.id", target = "propertyId")
    @Mapping(source = "property.isTopPromoted", target = "isTopPromoted")
    @Mapping(source = "property.topPromotedUntil", target = "topPromotedUntil")
    @Mapping(source = "chargedAmount", target = "chargedAmount")
    @Mapping(source = "balanceAfter", target = "balanceAfter")
    @Mapping(source = "currency", target = "currency")
    TopPromotionPurchaseResponseDto toTopPromotionPurchaseResponse(
            Property property,
            BigDecimal chargedAmount,
            BigDecimal balanceAfter,
            String currency
    );

    @Mapping(source = "user.subscriptionPlan", target = "subscriptionPlan")
    @Mapping(source = "user.subscriptionActiveUntil", target = "subscriptionActiveUntil")
    @Mapping(source = "chargedAmount", target = "chargedAmount")
    @Mapping(source = "balanceAfter", target = "balanceAfter")
    @Mapping(source = "currency", target = "currency")
    SubscriptionPurchaseResponseDto toSubscriptionPurchaseResponse(
            User user,
            BigDecimal chargedAmount,
            BigDecimal balanceAfter,
            String currency
    );

    default TopPromotionPackageDto toTopPromotionPackageDto(TopPromotionPackageType packageType, String currency) {
        if (packageType == null) {
            return null;
        }
        return new TopPromotionPackageDto(
                packageType,
                packageType.getDurationDays(),
                packageType.getPrice(),
                currency
        );
    }

    default List<TopPromotionPackageDto> toTopPromotionPackageDtos(List<TopPromotionPackageType> packageTypes, String currency) {
        if (packageTypes == null || packageTypes.isEmpty()) {
            return List.of();
        }
        return packageTypes.stream()
                .map(packageType -> toTopPromotionPackageDto(packageType, currency))
                .toList();
    }

    default SubscriptionPackageDto toSubscriptionPackageDto(SubscriptionPackageType packageType, String currency) {
        if (packageType == null) {
            return null;
        }
        return new SubscriptionPackageDto(
                packageType,
                packageType.getPlan(),
                packageType.getDurationDays(),
                packageType.getPrice(),
                currency
        );
    }

    default List<SubscriptionPackageDto> toSubscriptionPackageDtos(
            List<SubscriptionPackageType> packageTypes,
            String currency
    ) {
        if (packageTypes == null || packageTypes.isEmpty()) {
            return List.of();
        }
        return packageTypes.stream()
                .map(packageType -> toSubscriptionPackageDto(packageType, currency))
                .toList();
    }
}
