package com.rentify.core.scheduler;

import com.rentify.core.enums.SubscriptionPlan;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class PromotionStatusScheduler {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(PromotionStatusScheduler.class);

    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional
    public void expirePromotionsAndSubscriptions() {
        ZonedDateTime now = ZonedDateTime.now();
        int topExpired = propertyRepository.deactivateExpiredTopPromotions(now);
        int subscriptionsExpired = userRepository.resetExpiredSubscriptions(SubscriptionPlan.FREE, now);

        if (topExpired > 0 || subscriptionsExpired > 0) {
            logger.info("Expired promotions job: top={}, subscriptions={}", topExpired, subscriptionsExpired);
        }
    }
}
