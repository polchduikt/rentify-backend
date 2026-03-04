package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PropertyRuleDto(
        Long id,
        @JsonProperty("petsAllowed") Boolean petsAllowed,
        @JsonProperty("smokingAllowed") Boolean smokingAllowed,
        @JsonProperty("partiesAllowed") Boolean partiesAllowed,
        @JsonProperty("additionalRules") String additionalRules
) {}
