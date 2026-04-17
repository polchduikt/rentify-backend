package com.rentify.core.service.impl;

import com.rentify.core.dto.wallet.WalletBalanceDto;
import com.rentify.core.dto.wallet.TopUpOptionDto;
import com.rentify.core.dto.wallet.WalletTopUpRequestDto;
import com.rentify.core.dto.wallet.WalletTransactionDto;
import com.rentify.core.entity.User;
import com.rentify.core.entity.WalletTransaction;
import com.rentify.core.enums.WalletReferenceType;
import com.rentify.core.enums.WalletTransactionDirection;
import com.rentify.core.enums.WalletTransactionType;
import com.rentify.core.mapper.WalletTransactionMapper;
import com.rentify.core.repository.UserRepository;
import com.rentify.core.repository.WalletTransactionRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.CurrencyResolver;
import com.rentify.core.service.WalletNormalizationService;
import com.rentify.core.service.WalletService;
import com.rentify.core.validation.WalletValidator;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rentify.core.exception.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletTransactionMapper walletTransactionMapper;
    private final WalletNormalizationService walletNormalizationService;
    private final CurrencyResolver currencyResolver;
    private final WalletValidator walletValidator;

    @org.springframework.beans.factory.annotation.Value("${application.wallet.top-up-options:300.00,500.00,1000.00}")
    private List<BigDecimal> walletTopUpOptions = List.of(
            new BigDecimal("300.00"),
            new BigDecimal("500.00"),
            new BigDecimal("1000.00")
    );

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
        return walletTransactionMapper.toWalletBalanceDto(user, resolveCurrency());
    }

    @Override
    @Transactional
    public WalletBalanceDto topUpBalance(WalletTopUpRequestDto request) {
        User user = authenticationService.getCurrentUser();
        walletNormalizationService.normalizeWalletDefaults(user);
        walletNormalizationService.normalizeSubscription(user, ZonedDateTime.now());
        BigDecimal amount = walletValidator.normalizeAmount(request.amount(), resolveAllowedTopUpAmounts());

        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);

        WalletTransaction transaction = WalletTransaction.builder()
                .user(user)
                .direction(WalletTransactionDirection.CREDIT)
                .type(WalletTransactionType.TOP_UP)
                .amount(amount)
                .currency(resolveCurrency())
                .description("Mock wallet top-up")
                .referenceType(WalletReferenceType.WALLET)
                .build();
        walletTransactionRepository.save(transaction);
        log.info("Wallet top-up completed: userId={}, amount={}, currency={}, newBalance={}",
                user.getId(), amount, resolveCurrency(), user.getBalance());

        return walletTransactionMapper.toWalletBalanceDto(user, resolveCurrency());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WalletTransactionDto> getMyTransactions(Pageable pageable) {
        User user = authenticationService.getCurrentUser();
        walletNormalizationService.normalizeWalletDefaults(user);
        walletNormalizationService.normalizeSubscription(user, ZonedDateTime.now());
        return walletTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(walletTransactionMapper::toDto);
    }

    @Override
    public List<TopUpOptionDto> getTopUpOptions() {
        String currency = resolveCurrency();
        return walletTransactionMapper.toTopUpOptionDtos(resolveAllowedTopUpAmounts(), currency);
    }

    private List<BigDecimal> resolveAllowedTopUpAmounts() {
        List<BigDecimal> configured = walletTopUpOptions;
        if (configured == null || configured.isEmpty()) {
            throw DomainException.internal("WALLET_TOPUP_OPTIONS_NOT_CONFIGURED", "Wallet top-up options are not configured");
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
            throw DomainException.internal("WALLET_TOPUP_OPTIONS_INVALID", "Wallet top-up options must be positive values");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveCurrency() {
        return currencyResolver.resolveDefaultCurrency();
    }
}
