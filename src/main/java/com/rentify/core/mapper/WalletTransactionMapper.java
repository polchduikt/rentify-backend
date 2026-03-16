package com.rentify.core.mapper;

import com.rentify.core.dto.wallet.WalletTransactionDto;
import com.rentify.core.entity.WalletTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletTransactionMapper {

    @Mapping(source = "user.id", target = "userId")
    WalletTransactionDto toDto(WalletTransaction transaction);
}
