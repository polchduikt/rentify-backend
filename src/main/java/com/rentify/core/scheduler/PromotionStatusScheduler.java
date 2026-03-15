package com.rentify.core.scheduler;

import com.rentify.core.enums.SubscriptionPlan;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class PromotionStatusScheduler {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Kiev");

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(PromotionStatusScheduler.class);

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncPromotionsOnStartup() {
        expire("startup");
    }

    @Scheduled(cron = "0 */5 * * * ?", zone = "Europe/Kiev")
    @Transactional
    public void expirePromotionsAndSubscriptions() {
        expire("schedule");
    }

    private void expire(String trigger) {
        ZonedDateTime now = ZonedDateTime.now(BUSINESS_ZONE);
        int topExpired = propertyRepository.deactivateExpiredTopPromotions(now);
        int subscriptionsExpired = userRepository.resetExpiredSubscriptions(SubscriptionPlan.FREE, now);

        if (topExpired > 0 || subscriptionsExpired > 0) {
            logger.info("Expired promotions job [{}]: top={}, subscriptions={}", trigger, topExpired, subscriptionsExpired);
        }
    }
}
