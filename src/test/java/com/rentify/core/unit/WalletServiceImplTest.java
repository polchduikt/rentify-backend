package com.rentify.core.unit;

import com.rentify.core.dto.wallet.WalletBalanceDto;
import com.rentify.core.dto.wallet.WalletTopUpRequestDto;
import com.rentify.core.dto.wallet.WalletTransactionDto;
import com.rentify.core.entity.User;
import com.rentify.core.entity.WalletTransaction;
import com.rentify.core.enums.SubscriptionPlan;
import com.rentify.core.enums.WalletTransactionDirection;
import com.rentify.core.enums.WalletTransactionType;
import com.rentify.core.mapper.WalletTransactionMapper;
import com.rentify.core.repository.UserRepository;
import com.rentify.core.repository.WalletTransactionRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.WalletNormalizationService;
import com.rentify.core.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock private AuthenticationService authenticationService;
    @Mock private UserRepository userRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private WalletTransactionMapper walletTransactionMapper;
    @Mock private WalletNormalizationService walletNormalizationService;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .balance(new BigDecimal("100.00"))
                .subscriptionPlan(SubscriptionPlan.FREE)
                .subscriptionActiveUntil(null)
                .build();
    }

    @Nested
    @DisplayName("getMyWallet()")
    class GetMyWalletTests {

        @Test
        void shouldSaveUser_whenNormalizationChangedState() {
            when(authenticationService.getCurrentUser()).thenReturn(user);
            when(walletNormalizationService.normalizeWalletDefaults(user)).thenReturn(true);
            when(walletNormalizationService.normalizeSubscription(any(User.class), any(ZonedDateTime.class))).thenReturn(false);

            WalletBalanceDto result = walletService.getMyWallet();

            assertThat(result.balance()).isEqualByComparingTo("100.00");
            assertThat(result.currency()).isEqualTo("UAH");
            verify(userRepository).save(user);
        }

        @Test
        void shouldNotSaveUser_whenNormalizationDidNotChangeState() {
            when(authenticationService.getCurrentUser()).thenReturn(user);
            when(walletNormalizationService.normalizeWalletDefaults(user)).thenReturn(false);
            when(walletNormalizationService.normalizeSubscription(any(User.class), any(ZonedDateTime.class))).thenReturn(false);

            walletService.getMyWallet();

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("topUpBalance()")
    class TopUpBalanceTests {

        @Test
        void shouldThrowIllegalArgument_whenAmountMissing() {
            when(authenticationService.getCurrentUser()).thenReturn(user);

            assertThatThrownBy(() -> walletService.topUpBalance(new WalletTopUpRequestDto(null)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Top-up amount is required");
        }

        @Test
        void shouldThrowIllegalArgument_whenAmountNotPositive() {
            when(authenticationService.getCurrentUser()).thenReturn(user);

            assertThatThrownBy(() -> walletService.topUpBalance(new WalletTopUpRequestDto(BigDecimal.ZERO)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Top-up amount must be greater than zero");
        }

        @Test
        void shouldTopUpBalanceAndCreateTransaction() {
            when(authenticationService.getCurrentUser()).thenReturn(user);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            WalletBalanceDto result = walletService.topUpBalance(new WalletTopUpRequestDto(new BigDecimal("100.126")));
            ArgumentCaptor<WalletTransaction> transactionCaptor = ArgumentCaptor.forClass(WalletTransaction.class);

            assertThat(result.balance()).isEqualByComparingTo("200.13");
            assertThat(result.currency()).isEqualTo("UAH");
            verify(walletTransactionRepository).save(transactionCaptor.capture());
            assertThat(transactionCaptor.getValue().getDirection()).isEqualTo(WalletTransactionDirection.CREDIT);
            assertThat(transactionCaptor.getValue().getType()).isEqualTo(WalletTransactionType.TOP_UP);
            assertThat(transactionCaptor.getValue().getAmount()).isEqualByComparingTo("100.13");
            assertThat(transactionCaptor.getValue().getReferenceType()).isEqualTo("WALLET");
        }
    }

    @Nested
    @DisplayName("getMyTransactions()")
    class GetMyTransactionsTests {

        @Test
        void shouldReturnMappedTransactionsPage() {
            Pageable pageable = PageRequest.of(0, 10);
            WalletTransaction transaction = WalletTransaction.builder().id(1L).user(user).build();
            WalletTransactionDto dto = new WalletTransactionDto(
                    1L, 1L, WalletTransactionDirection.CREDIT, WalletTransactionType.TOP_UP,
                    new BigDecimal("100.00"), "UAH", "Top up", "WALLET", null, ZonedDateTime.now()
            );
            Page<WalletTransaction> page = new PageImpl<>(List.of(transaction), pageable, 1);

            when(authenticationService.getCurrentUser()).thenReturn(user);
            when(walletTransactionRepository.findAllByUserIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(page);
            when(walletTransactionMapper.toDto(transaction)).thenReturn(dto);

            Page<WalletTransactionDto> result = walletService.getMyTransactions(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("getTopUpOptions()")
    class GetTopUpOptionsTests {

        @Test
        void shouldReturnConfiguredTopUpOptions() {
            List<BigDecimal> options = walletService.getTopUpOptions();

            assertThat(options).containsExactly(
                    new BigDecimal("300.00"),
                    new BigDecimal("500.00"),
                    new BigDecimal("1000.00")
            );
        }
    }
}
