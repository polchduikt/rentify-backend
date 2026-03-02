package com.rentify.core.dto;

public record PropertyRuleDto(
        Boolean petsAllowed,
        Boolean smokingAllowed,
        Boolean partiesAllowed,
        String additionalRules
) {}