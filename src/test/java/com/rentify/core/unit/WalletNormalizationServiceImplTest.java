package com.rentify.core.unit;

import com.rentify.core.entity.User;
import com.rentify.core.enums.SubscriptionPlan;
import com.rentify.core.service.impl.WalletNormalizationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class WalletNormalizationServiceImplTest {

    private final WalletNormalizationServiceImpl walletNormalizationService = new WalletNormalizationServiceImpl();

    @Nested
    @DisplayName("normalizeWalletDefaults()")
    class NormalizeWalletDefaultsTests {

        @Test
        void shouldSetDefaultsAndReturnTrue_whenValuesMissing() {
            User user = User.builder().balance(null).subscriptionPlan(null).build();

            boolean changed = walletNormalizationService.normalizeWalletDefaults(user);

            assertThat(changed).isTrue();
            assertThat(user.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(user.getSubscriptionPlan()).isEqualTo(SubscriptionPlan.FREE);
        }

        @Test
        void shouldReturnFalse_whenDefaultsAlreadySet() {
            User user = User.builder()
                    .balance(new BigDecimal("100.00"))
                    .subscriptionPlan(SubscriptionPlan.BASIC)
                    .build();

            boolean changed = walletNormalizationService.normalizeWalletDefaults(user);

            assertThat(changed).isFalse();
            assertThat(user.getBalance()).isEqualByComparingTo("100.00");
            assertThat(user.getSubscriptionPlan()).isEqualTo(SubscriptionPlan.BASIC);
        }
    }

    @Nested
    @DisplayName("normalizeSubscription()")
    class NormalizeSubscriptionTests {

        @Test
        void shouldResetSubscriptionAndReturnTrue_whenSubscriptionExpired() {
            ZonedDateTime now = ZonedDateTime.now();
            User user = User.builder()
                    .subscriptionPlan(SubscriptionPlan.PREMIUM)
                    .subscriptionActiveUntil(now.minusMinutes(1))
                    .build();

            boolean changed = walletNormalizationService.normalizeSubscription(user, now);

            assertThat(changed).isTrue();
            assertThat(user.getSubscriptionPlan()).isEqualTo(SubscriptionPlan.FREE);
            assertThat(user.getSubscriptionActiveUntil()).isNull();
        }

        @Test
        void shouldReturnFalse_whenSubscriptionStillActive() {
            ZonedDateTime now = ZonedDateTime.now();
            User user = User.builder()
                    .subscriptionPlan(SubscriptionPlan.BASIC)
                    .subscriptionActiveUntil(now.plusDays(1))
                    .build();

            boolean changed = walletNormalizationService.normalizeSubscription(user, now);

            assertThat(changed).isFalse();
            assertThat(user.getSubscriptionPlan()).isEqualTo(SubscriptionPlan.BASIC);
            assertThat(user.getSubscriptionActiveUntil()).isAfter(now);
        }
    }
}
