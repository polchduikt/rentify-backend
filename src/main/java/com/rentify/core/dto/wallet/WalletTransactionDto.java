package com.rentify.core.dto.wallet;

import com.rentify.core.enums.WalletTransactionDirection;
import com.rentify.core.enums.WalletTransactionType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Wallet transaction payload")
public record WalletTransactionDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "User id", example = "42")
        Long userId,
        @Schema(description = "Direction", example = "IN")
        WalletTransactionDirection direction,
        @Schema(description = "Type", example = "TOP_UP")
        WalletTransactionType type,
        @Schema(description = "Amount", example = "100.0")
        BigDecimal amount,
        @Schema(description = "Currency", example = "Sample value")
        String currency,
        @Schema(description = "Description", example = "Spacious apartment with balcony and modern renovation.")
        String description,
        @Schema(description = "Reference type", example = "apartment")
        String referenceType,
        @Schema(description = "Reference id", example = "42")
        Long referenceId,
        ZonedDateTime createdAt
) {
}
