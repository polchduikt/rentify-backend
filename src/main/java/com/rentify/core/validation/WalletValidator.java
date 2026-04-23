package com.rentify.core.validation;

import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import com.rentify.core.exception.DomainException;

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
            throw DomainException.badRequest("WALLET_TOPUP_AMOUNT_REQUIRED", "Top-up amount is required");
        }
        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw DomainException.badRequest("WALLET_TOPUP_AMOUNT_INVALID", "Top-up amount must be greater than zero");
        }
        if (!allowedAmounts.contains(normalizedAmount)) {
            throw DomainException.badRequest(
                    "WALLET_TOPUP_AMOUNT_NOT_ALLOWED",
                    "Top-up amount is not allowed",
                    java.util.Map.of("amount", normalizedAmount.toPlainString())
            );
        }
        return normalizedAmount;
    }
}
