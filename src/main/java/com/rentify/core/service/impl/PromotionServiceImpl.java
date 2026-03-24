package com.rentify.core.service.impl;

import com.rentify.core.dto.promotion.SubscriptionPackageDto;
import com.rentify.core.dto.promotion.SubscriptionPurchaseResponseDto;
import com.rentify.core.dto.promotion.TopPromotionPackageDto;
import com.rentify.core.dto.promotion.TopPromotionPurchaseResponseDto;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.entity.WalletTransaction;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.SubscriptionPackageType;
import com.rentify.core.enums.TopPromotionPackageType;
import com.rentify.core.enums.WalletTransactionDirection;
import com.rentify.core.enums.WalletTransactionType;
import com.rentify.core.mapper.PromotionMapper;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.repository.UserRepository;
import com.rentify.core.repository.WalletTransactionRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.PromotionService;
import com.rentify.core.service.WalletNormalizationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private static final String UAH = "UAH";

    private final AuthenticationService authenticationService;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletNormalizationService walletNormalizationService;
    private final PromotionMapper promotionMapper;

    @Override
    @Transactional
    public TopPromotionPurchaseResponseDto purchaseTopPromotion(Long propertyId, TopPromotionPackageType packageType) {
        if (packageType == null) {
            throw new IllegalArgumentException("Top promotion package is required");
        }
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        User user = loadCurrentUserForUpdate();
        walletNormalizationService.normalizeWalletDefaults(user);

        if (!property.getHost().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only promote your own properties");
        }
        if (property.getStatus() != PropertyStatus.ACTIVE) {
            throw new IllegalStateException("Only active properties can be promoted");
        }
        BigDecimal price = packageType.getPrice();
        ensureSufficientBalance(user, price);

        user.setBalance(user.getBalance().subtract(price));
        userRepository.save(user);

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime baseTime = property.getTopPromotedUntil() != null && property.getTopPromotedUntil().isAfter(now)
                ? property.getTopPromotedUntil()
                : now;
        ZonedDateTime topUntil = baseTime.plusDays(packageType.getDurationDays());

        property.setIsTopPromoted(true);
        property.setTopPromotedUntil(topUntil);
        propertyRepository.save(property);

        WalletTransaction transaction = WalletTransaction.builder()
                .user(user)
                .direction(WalletTransactionDirection.DEBIT)
                .type(WalletTransactionType.TOP_PROMOTION)
                .amount(price)
                .currency(UAH)
                .description("Top promotion for property #" + propertyId + " (" + packageType.name() + ")")
                .referenceType("PROPERTY")
                .referenceId(propertyId)
                .build();
        walletTransactionRepository.save(transaction);

        return promotionMapper.toTopPromotionPurchaseResponse(
                property,
                price,
                user.getBalance(),
                UAH
        );
    }

    @Override
    @Transactional
    public SubscriptionPurchaseResponseDto purchaseSubscription(SubscriptionPackageType packageType) {
        if (packageType == null) {
            throw new IllegalArgumentException("Subscription package is required");
        }
        User user = loadCurrentUserForUpdate();
        walletNormalizationService.normalizeWalletDefaults(user);
        BigDecimal price = packageType.getPrice();
        ensureSufficientBalance(user, price);

        user.setBalance(user.getBalance().subtract(price));

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime currentUntil = user.getSubscriptionActiveUntil();
        ZonedDateTime baseTime = currentUntil != null && currentUntil.isAfter(now) ? currentUntil : now;
        ZonedDateTime activeUntil = baseTime.plusDays(packageType.getDurationDays());

        user.setSubscriptionPlan(packageType.getPlan());
        user.setSubscriptionActiveUntil(activeUntil);
        userRepository.save(user);

        WalletTransaction transaction = WalletTransaction.builder()
                .user(user)
                .direction(WalletTransactionDirection.DEBIT)
                .type(WalletTransactionType.SUBSCRIPTION)
                .amount(price)
                .currency(UAH)
                .description("Subscription purchase (" + packageType.name() + ")")
                .referenceType("SUBSCRIPTION")
                .build();
        walletTransactionRepository.save(transaction);

        return promotionMapper.toSubscriptionPurchaseResponse(
                user,
                price,
                user.getBalance(),
                UAH
        );
    }

    @Override
    public List<TopPromotionPackageDto> getTopPromotionPackages() {
        return promotionMapper.toTopPromotionPackageDtos(
                Arrays.asList(TopPromotionPackageType.values()),
                UAH
        );
    }

    @Override
    public List<SubscriptionPackageDto> getSubscriptionPackages() {
        return promotionMapper.toSubscriptionPackageDtos(
                Arrays.asList(SubscriptionPackageType.values()),
                UAH
        );
    }

    private void ensureSufficientBalance(User user, BigDecimal price) {
        if (user.getBalance().compareTo(price) < 0) {
            throw new IllegalStateException("Insufficient balance. Please top up your wallet.");
        }
    }

    private User loadCurrentUserForUpdate() {
        User currentUser = authenticationService.getCurrentUser();
        return userRepository.findByIdForUpdate(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
