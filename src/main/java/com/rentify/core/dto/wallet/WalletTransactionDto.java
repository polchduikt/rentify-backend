package com.rentify.core.dto.wallet;

import com.rentify.core.enums.WalletTransactionDirection;
import com.rentify.core.enums.WalletTransactionType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record WalletTransactionDto(
        Long id,
        Long userId,
        WalletTransactionDirection direction,
        WalletTransactionType type,
        BigDecimal amount,
        String currency,
        String description,
        String referenceType,
        Long referenceId,
        ZonedDateTime createdAt
) {
}
