package com.rentify.core.controller;

import com.rentify.core.dto.wallet.WalletBalanceDto;
import com.rentify.core.dto.wallet.TopUpOptionDto;
import com.rentify.core.dto.wallet.WalletTopUpRequestDto;
import com.rentify.core.dto.wallet.WalletTransactionDto;
import com.rentify.core.service.WalletService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Wallet balance, top-up and transaction history")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    @Operation(
            summary = "Get current user wallet",
            description = "Returns wallet balance and subscription-related monetary state for current user."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Wallet retrieved",
            content = @Content(schema = @Schema(implementation = WalletBalanceDto.class))
    )
    public ResponseEntity<WalletBalanceDto> getMyWallet() {
        return ResponseEntity.ok(walletService.getMyWallet());
    }

    @PostMapping("/transactions")
    @Operation(
            summary = "Top up wallet balance",
            description = "Adds funds to user wallet by selected supported amount."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Wallet topped up",
            content = @Content(schema = @Schema(implementation = WalletBalanceDto.class))
    )
    public ResponseEntity<WalletBalanceDto> topUp(@Valid @RequestBody WalletTopUpRequestDto request) {
        return ResponseEntity.ok(walletService.topUpBalance(request));
    }

    @GetMapping("/transactions")
    @Operation(
            summary = "Get wallet transactions",
            description = "Returns paginated wallet transaction history for current user."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Transactions retrieved",
            content = @Content(schema = @Schema(implementation = WalletTransactionDto.class))
    )
    public ResponseEntity<Page<WalletTransactionDto>> getMyTransactions(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(walletService.getMyTransactions(pageable));
    }

    @GetMapping("/transaction-amount-options")
    @Operation(
            summary = "Get allowed top-up amounts",
            description = "Returns fixed list of amounts that can be used for wallet top-up."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Top-up options retrieved",
            content = @Content(schema = @Schema(implementation = TopUpOptionDto.class))
    )
    public ResponseEntity<List<TopUpOptionDto>> getTopUpOptions() {
        return ResponseEntity.ok(walletService.getTopUpOptions());
    }
}
