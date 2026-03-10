package com.rentify.core.service;

import com.rentify.core.entity.User;

import java.time.ZonedDateTime;

public interface WalletNormalizationService {
    boolean normalizeWalletDefaults(User user);
    boolean normalizeSubscription(User user, ZonedDateTime now);
}
