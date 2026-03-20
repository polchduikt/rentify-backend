package com.rentify.core.service.impl;

import com.rentify.core.config.WalletProperties;
import com.rentify.core.dto.wallet.WalletBalanceDto;
import com.rentify.core.dto.wallet.TopUpOptionDto;
import com.rentify.core.dto.wallet.WalletTopUpRequestDto;
import com.rentify.core.dto.wallet.WalletTransactionDto;
import com.rentify.core.entity.User;
import com.rentify.core.entity.WalletTransaction;
import com.rentify.core.enums.WalletTransactionDirection;
import com.rentify.core.enums.WalletTransactionType;
import com.rentify.core.mapper.WalletTransactionMapper;
import com.rentify.core.repository.UserRepository;
import com.rentify.core.repository.WalletTransactionRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.WalletNormalizationService;
import com.rentify.core.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletTransactionMapper walletTransactionMapper;
    private final WalletNormalizationService walletNormalizationService;
    private final WalletProperties walletProperties;

    @Override
    @Transactional
    public WalletBalanceDto getMyWallet() {
        User user = authenticationService.getCurrentUser();
        boolean changed = false;
        changed |= walletNormalizationService.normalizeWalletDefaults(user);
        changed |= walletNormalizationService.normalizeSubscription(user, ZonedDateTime.now());
        if (changed) {
            userRepository.save(user);
        }
        return toWalletBalanceDto(user);
    }

    @Override
    @Transactional
    public WalletBalanceDto topUpBalance(WalletTopUpRequestDto request) {
        User user = authenticationService.getCurrentUser();
        walletNormalizationService.normalizeWalletDefaults(user);
        walletNormalizationService.normalizeSubscription(user, ZonedDateTime.now());
        BigDecimal amount = normalizeAmount(request.amount());

        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);

        WalletTransaction transaction = WalletTransaction.builder()
                .user(user)
                .direction(WalletTransactionDirection.CREDIT)
                .type(WalletTransactionType.TOP_UP)
                .amount(amount)
                .currency(resolveCurrency())
                .description("Mock wallet top-up")
                .referenceType("WALLET")
                .build();
        walletTransactionRepository.save(transaction);

        return toWalletBalanceDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WalletTransactionDto> getMyTransactions(Pageable pageable) {
        User user = authenticationService.getCurrentUser();
        return walletTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(walletTransactionMapper::toDto);
    }

    @Override
    public List<TopUpOptionDto> getTopUpOptions() {
        String currency = resolveCurrency();
        return resolveAllowedTopUpAmounts().stream()
                .map(amount -> new TopUpOptionDto(amount, currency))
                .toList();
    }

    private WalletBalanceDto toWalletBalanceDto(User user) {
        return new WalletBalanceDto(
                user.getBalance(),
                resolveCurrency(),
                user.getSubscriptionPlan(),
                user.getSubscriptionActiveUntil()
        );
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Top-up amount is required");
        }
        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be greater than zero");
        }
        if (!resolveAllowedTopUpAmounts().contains(normalizedAmount)) {
            throw new IllegalArgumentException("Top-up amount is not allowed");
        }
        return normalizedAmount;
    }

    private List<BigDecimal> resolveAllowedTopUpAmounts() {
        List<BigDecimal> configured = walletProperties.getTopUpOptions();
        if (configured == null || configured.isEmpty()) {
            throw new IllegalStateException("Wallet top-up options are not configured");
        }
        return configured.stream()
                .filter(Objects::nonNull)
                .map(this::normalizeConfiguredAmount)
                .distinct()
                .sorted()
                .toList();
    }

    private BigDecimal normalizeConfiguredAmount(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Wallet top-up options must be positive values");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveCurrency() {
        String currency = walletProperties.getCurrency();
        if (currency == null || currency.isBlank()) {
            throw new IllegalStateException("Wallet currency is not configured");
        }
        return currency.trim();
    }
}
