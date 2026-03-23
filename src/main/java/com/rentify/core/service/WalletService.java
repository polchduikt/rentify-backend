package com.rentify.core.service;

import com.rentify.core.dto.wallet.WalletBalanceDto;
import com.rentify.core.dto.wallet.TopUpOptionDto;
import com.rentify.core.dto.wallet.WalletTopUpRequestDto;
import com.rentify.core.dto.wallet.WalletTransactionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WalletService {
    WalletBalanceDto getMyWallet();
    WalletBalanceDto topUpBalance(WalletTopUpRequestDto request);
    Page<WalletTransactionDto> getMyTransactions(Pageable pageable);
    List<TopUpOptionDto> getTopUpOptions();
}
