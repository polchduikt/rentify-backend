package com.rentify.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "application.wallet")
public class WalletProperties {

    private String currency = "UAH";
    private List<BigDecimal> topUpOptions = List.of(
            new BigDecimal("300.00"),
            new BigDecimal("500.00"),
            new BigDecimal("1000.00")
    );
}
