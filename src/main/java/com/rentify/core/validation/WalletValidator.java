package com.rentify.core.validation;

import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class WalletValidator extends AbstractValidator {

    public WalletValidator(Validator validator) {
        super(validator);
    }

    public BigDecimal normalizeAmount(BigDecimal amount, List<BigDecimal> allowedAmounts) {
        if (amount == null) {
            throw new IllegalArgumentException("Top-up amount is required");
        }
        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be greater than zero");
        }
        if (!allowedAmounts.contains(normalizedAmount)) {
            throw new IllegalArgumentException("Top-up amount is not allowed");
        }
        return normalizedAmount;
    }
}
