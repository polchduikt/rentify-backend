package com.rentify.core.dto.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Wallet top up request payload")
public record WalletTopUpRequestDto(
        @NotNull(message = "Top-up amount is required")
        @DecimalMin(value = "1.00", message = "Top-up amount must be at least 1.00")
        BigDecimal amount
) {
}
