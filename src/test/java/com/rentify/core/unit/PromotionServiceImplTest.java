package com.rentify.core.unit;

import com.rentify.core.dto.promotion.SubscriptionPackageDto;
import com.rentify.core.dto.promotion.SubscriptionPurchaseResponseDto;
import com.rentify.core.dto.promotion.TopPromotionPackageDto;
import com.rentify.core.dto.promotion.TopPromotionPurchaseResponseDto;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.User;
import com.rentify.core.entity.WalletTransaction;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.SubscriptionPackageType;
import com.rentify.core.enums.SubscriptionPlan;
import com.rentify.core.enums.TopPromotionPackageType;
import com.rentify.core.enums.WalletReferenceType;
import com.rentify.core.enums.WalletTransactionDirection;
import com.rentify.core.enums.WalletTransactionType;
import com.rentify.core.mapper.PromotionMapper;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.repository.UserRepository;
import com.rentify.core.repository.WalletTransactionRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.WalletNormalizationService;
import com.rentify.core.service.impl.PromotionServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceImplTest {

    @Mock private AuthenticationService authenticationService;
    @Mock private PropertyRepository propertyRepository;
    @Mock private UserRepository userRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private WalletNormalizationService walletNormalizationService;
    @Mock private PromotionMapper promotionMapper;

    @InjectMocks
    private PromotionServiceImpl promotionService;

    private User host;
    private User otherUser;
    private Property property;

    @BeforeEach
    void setUp() {
        host = User.builder()
                .id(1L)
                .balance(new BigDecimal("1000.00"))
                .subscriptionPlan(SubscriptionPlan.FREE)
                .build();
        otherUser = User.builder()
                .id(2L)
                .balance(new BigDecimal("1000.00"))
                .subscriptionPlan(SubscriptionPlan.FREE)
                .build();

        property = Property.builder()
                .id(10L)
                .host(host)
                .status(PropertyStatus.ACTIVE)
                .isTopPromoted(false)
                .build();
    }

    @Nested
    @DisplayName("purchaseTopPromotion()")
    class PurchaseTopPromotionTests {

        @Test
        void shouldThrowIllegalArgument_whenPackageMissing() {
            assertThatThrownBy(() -> promotionService.purchaseTopPromotion(10L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Top promotion package is required");
        }

        @Test
        void shouldThrowEntityNotFound_whenPropertyMissing() {
            when(propertyRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> promotionService.purchaseTopPromotion(10L, TopPromotionPackageType.TOP_7_DAYS))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("Property not found");
        }

        @Test
        void shouldThrowAccessDenied_whenUserNotPropertyHost() {
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(authenticationService.getCurrentUser()).thenReturn(otherUser);
            when(userRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(otherUser));

            assertThatThrownBy(() -> promotionService.purchaseTopPromotion(10L, TopPromotionPackageType.TOP_7_DAYS))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You can only promote your own properties");
        }

        @Test
        void shouldThrowIllegalState_whenPropertyNotActive() {
            property.setStatus(PropertyStatus.INACTIVE);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(authenticationService.getCurrentUser()).thenReturn(host);
            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(host));

            assertThatThrownBy(() -> promotionService.purchaseTopPromotion(10L, TopPromotionPackageType.TOP_7_DAYS))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Only active properties can be promoted");
        }

        @Test
        void shouldThrowIllegalState_whenBalanceInsufficient() {
            host.setBalance(new BigDecimal("50.00"));
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(authenticationService.getCurrentUser()).thenReturn(host);
            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(host));

            assertThatThrownBy(() -> promotionService.purchaseTopPromotion(10L, TopPromotionPackageType.TOP_7_DAYS))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Insufficient balance. Please top up your wallet.");
        }

        @Test
        void shouldPurchaseTopPromotionAndExtendFromExistingTopUntil() {
            ZonedDateTime existingTopUntil = ZonedDateTime.now().plusDays(2);
            property.setTopPromotedUntil(existingTopUntil);
            property.setIsTopPromoted(false);
            when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
            when(authenticationService.getCurrentUser()).thenReturn(host);
            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(host));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(promotionMapper.toTopPromotionPurchaseResponse(any(Property.class), any(BigDecimal.class), any(BigDecimal.class), anyString()))
                    .thenAnswer(invocation -> {
                        Property mappedProperty = invocation.getArgument(0);
                        BigDecimal chargedAmount = invocation.getArgument(1);
                        BigDecimal balanceAfter = invocation.getArgument(2);
                        String currency = invocation.getArgument(3);
                        return new TopPromotionPurchaseResponseDto(
                                mappedProperty.getId(),
                                mappedProperty.getIsTopPromoted(),
                                mappedProperty.getTopPromotedUntil(),
                                chargedAmount,
                                balanceAfter,
                                currency
                        );
                    });

            TopPromotionPurchaseResponseDto result =
                    promotionService.purchaseTopPromotion(10L, TopPromotionPackageType.TOP_7_DAYS);
            ArgumentCaptor<WalletTransaction> transactionCaptor = ArgumentCaptor.forClass(WalletTransaction.class);

            assertThat(result.propertyId()).isEqualTo(10L);
            assertThat(result.isTopPromoted()).isTrue();
            assertThat(result.topPromotedUntil()).isEqualTo(existingTopUntil.plusDays(7));
            assertThat(result.chargedAmount()).isEqualByComparingTo("99.00");
            assertThat(result.balanceAfter()).isEqualByComparingTo("901.00");
            assertThat(result.currency()).isEqualTo("UAH");
            verify(walletNormalizationService).normalizeWalletDefaults(host);
            verify(walletTransactionRepository).save(transactionCaptor.capture());
            assertThat(transactionCaptor.getValue().getDirection()).isEqualTo(WalletTransactionDirection.DEBIT);
            assertThat(transactionCaptor.getValue().getType()).isEqualTo(WalletTransactionType.TOP_PROMOTION);
            assertThat(transactionCaptor.getValue().getReferenceType()).isEqualTo(WalletReferenceType.PROPERTY);
            assertThat(transactionCaptor.getValue().getReferenceId()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("purchaseSubscription()")
    class PurchaseSubscriptionTests {

        @Test
        void shouldThrowIllegalArgument_whenPackageMissing() {
            assertThatThrownBy(() -> promotionService.purchaseSubscription(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Subscription package is required");
        }

        @Test
        void shouldThrowIllegalState_whenBalanceInsufficient() {
            host.setBalance(new BigDecimal("100.00"));
            when(authenticationService.getCurrentUser()).thenReturn(host);
            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(host));

            assertThatThrownBy(() -> promotionService.purchaseSubscription(SubscriptionPackageType.BASIC_30_DAYS))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Insufficient balance. Please top up your wallet.");
        }

        @Test
        void shouldPurchaseSubscriptionAndExtendFromCurrentActiveUntil() {
            ZonedDateTime currentUntil = ZonedDateTime.now().plusDays(10);
            host.setSubscriptionPlan(SubscriptionPlan.BASIC);
            host.setSubscriptionActiveUntil(currentUntil);
            when(authenticationService.getCurrentUser()).thenReturn(host);
            when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(host));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(promotionMapper.toSubscriptionPurchaseResponse(any(User.class), any(BigDecimal.class), any(BigDecimal.class), anyString()))
                    .thenAnswer(invocation -> {
                        User mappedUser = invocation.getArgument(0);
                        BigDecimal chargedAmount = invocation.getArgument(1);
                        BigDecimal balanceAfter = invocation.getArgument(2);
                        String currency = invocation.getArgument(3);
                        return new SubscriptionPurchaseResponseDto(
                                mappedUser.getSubscriptionPlan(),
                                mappedUser.getSubscriptionActiveUntil(),
                                chargedAmount,
                                balanceAfter,
                                currency
                        );
                    });

            SubscriptionPurchaseResponseDto result =
                    promotionService.purchaseSubscription(SubscriptionPackageType.PREMIUM_30_DAYS);
            ArgumentCaptor<WalletTransaction> transactionCaptor = ArgumentCaptor.forClass(WalletTransaction.class);

            assertThat(result.subscriptionPlan()).isEqualTo(SubscriptionPlan.PREMIUM);
            assertThat(result.subscriptionActiveUntil()).isEqualTo(currentUntil.plusDays(30));
            assertThat(result.chargedAmount()).isEqualByComparingTo("399.00");
            assertThat(result.balanceAfter()).isEqualByComparingTo("601.00");
            assertThat(result.currency()).isEqualTo("UAH");
            verify(walletNormalizationService).normalizeWalletDefaults(host);
            verify(walletTransactionRepository).save(transactionCaptor.capture());
            assertThat(transactionCaptor.getValue().getDirection()).isEqualTo(WalletTransactionDirection.DEBIT);
            assertThat(transactionCaptor.getValue().getType()).isEqualTo(WalletTransactionType.SUBSCRIPTION);
            assertThat(transactionCaptor.getValue().getReferenceType()).isEqualTo(WalletReferenceType.SUBSCRIPTION);
        }
    }

    @Nested
    @DisplayName("package listings")
    class PackageListingTests {

        @Test
        void shouldReturnAllTopPromotionPackages() {
            when(promotionMapper.toTopPromotionPackageDtos(any(), anyString()))
                    .thenAnswer(invocation -> {
                        @SuppressWarnings("unchecked")
                        List<TopPromotionPackageType> packageTypes = invocation.getArgument(0);
                        String currency = invocation.getArgument(1);
                        return packageTypes.stream()
                                .map(pkg -> new TopPromotionPackageDto(pkg, pkg.getDurationDays(), pkg.getPrice(), currency))
                                .toList();
                    });
            List<TopPromotionPackageDto> packages = promotionService.getTopPromotionPackages();

            assertThat(packages).hasSize(TopPromotionPackageType.values().length);
            assertThat(packages).allSatisfy(pkg -> assertThat(pkg.currency()).isEqualTo("UAH"));
        }

        @Test
        void shouldReturnAllSubscriptionPackages() {
            when(promotionMapper.toSubscriptionPackageDtos(any(), anyString()))
                    .thenAnswer(invocation -> {
                        @SuppressWarnings("unchecked")
                        List<SubscriptionPackageType> packageTypes = invocation.getArgument(0);
                        String currency = invocation.getArgument(1);
                        return packageTypes.stream()
                                .map(pkg -> new SubscriptionPackageDto(
                                        pkg,
                                        pkg.getPlan(),
                                        pkg.getDurationDays(),
                                        pkg.getPrice(),
                                        currency
                                ))
                                .toList();
                    });
            List<SubscriptionPackageDto> packages = promotionService.getSubscriptionPackages();

            assertThat(packages).hasSize(SubscriptionPackageType.values().length);
            assertThat(packages).allSatisfy(pkg -> assertThat(pkg.currency()).isEqualTo("UAH"));
        }
    }
}
