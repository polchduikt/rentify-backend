package com.rentify.core.service.impl;

import com.rentify.core.entity.Property;
import com.rentify.core.exception.DomainException;
import com.rentify.core.service.CurrencyResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CurrencyResolverImpl implements CurrencyResolver {

    @Value("${application.wallet.currency:UAH}")
    private String defaultCurrency = "UAH";

    @Override
    public String resolvePropertyCurrency(Property property) {
        if (property != null
                && property.getPricing() != null
                && property.getPricing().getCurrency() != null
                && !property.getPricing().getCurrency().isBlank()) {
            return property.getPricing().getCurrency().trim();
        }
        return resolveDefaultCurrency();
    }

    @Override
    public String resolveDefaultCurrency() {
        String currency = defaultCurrency;
        if (currency == null || currency.isBlank()) {
            throw DomainException.internal("DEFAULT_CURRENCY_NOT_CONFIGURED", "Default currency is not configured");
        }
        return currency.trim();
    }
}
