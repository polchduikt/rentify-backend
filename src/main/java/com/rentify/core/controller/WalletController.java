package com.rentify.core.controller;

import com.rentify.core.dto.wallet.WalletBalanceDto;
import com.rentify.core.dto.wallet.WalletTopUpRequestDto;
import com.rentify.core.dto.wallet.WalletTransactionDto;
import com.rentify.core.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<WalletBalanceDto> getMyWallet() {
        return ResponseEntity.ok(walletService.getMyWallet());
    }

    @PostMapping("/top-up")
    public ResponseEntity<WalletBalanceDto> topUp(@Valid @RequestBody WalletTopUpRequestDto request) {
        return ResponseEntity.ok(walletService.topUpBalance(request));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<WalletTransactionDto>> getMyTransactions(Pageable pageable) {
        return ResponseEntity.ok(walletService.getMyTransactions(pageable));
    }

    @GetMapping("/top-up-options")
    public ResponseEntity<List<BigDecimal>> getTopUpOptions() {
        return ResponseEntity.ok(walletService.getTopUpOptions());
    }
}
