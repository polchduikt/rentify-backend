package com.rentify.core.service;

import com.rentify.core.entity.Property;

public interface CurrencyResolver {
    String resolvePropertyCurrency(Property property);
    String resolveDefaultCurrency();
}
