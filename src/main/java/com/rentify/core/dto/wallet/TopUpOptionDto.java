package com.rentify.core.dto.wallet;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Wallet top-up option payload")
public record TopUpOptionDto(
        @Schema(description = "Top-up amount", example = "500.00")
        BigDecimal amount,
        @Schema(description = "Currency", example = "UAH")
        String currency
) {
}
