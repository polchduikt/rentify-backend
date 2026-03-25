package com.rentify.core.mapper;

import com.rentify.core.dto.wallet.TopUpOptionDto;
import com.rentify.core.dto.wallet.WalletBalanceDto;
import com.rentify.core.dto.wallet.WalletTransactionDto;
import com.rentify.core.entity.User;
import com.rentify.core.entity.WalletTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(config = MapStructCentralConfig.class)
public interface WalletTransactionMapper {

    @Mapping(source = "user.id", target = "userId")
    WalletTransactionDto toDto(WalletTransaction transaction);

    List<WalletTransactionDto> toDtos(List<WalletTransaction> transactions);

    @Mapping(source = "user.balance", target = "balance")
    @Mapping(source = "currency", target = "currency")
    @Mapping(source = "user.subscriptionPlan", target = "subscriptionPlan")
    @Mapping(source = "user.subscriptionActiveUntil", target = "subscriptionActiveUntil")
    WalletBalanceDto toWalletBalanceDto(User user, String currency);

    default TopUpOptionDto toTopUpOptionDto(BigDecimal amount, String currency) {
        return new TopUpOptionDto(amount, currency);
    }

    default List<TopUpOptionDto> toTopUpOptionDtos(List<BigDecimal> amounts, String currency) {
        return amounts.stream()
                .map(amount -> toTopUpOptionDto(amount, currency))
                .toList();
    }
}
