package com.rentify.core.service.impl;

import com.rentify.core.entity.User;
import com.rentify.core.enums.SubscriptionPlan;
import com.rentify.core.service.WalletNormalizationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Service
public class WalletNormalizationServiceImpl implements WalletNormalizationService {

    @Override
    public boolean normalizeWalletDefaults(User user) {
        boolean changed = false;
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
            changed = true;
        }
        if (user.getSubscriptionPlan() == null) {
            user.setSubscriptionPlan(SubscriptionPlan.FREE);
            changed = true;
        }
        return changed;
    }

    @Override
    public boolean normalizeSubscription(User user, ZonedDateTime now) {
        if (user.getSubscriptionActiveUntil() != null && user.getSubscriptionActiveUntil().isBefore(now)) {
            user.setSubscriptionPlan(SubscriptionPlan.FREE);
            user.setSubscriptionActiveUntil(null);
            return true;
        }
        return false;
    }
}
