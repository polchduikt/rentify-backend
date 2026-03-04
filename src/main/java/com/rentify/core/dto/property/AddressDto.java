package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AddressDto(
        Long id,
        @Valid
        @NotNull(message = "Location is required")
        LocationDto location,
        @NotBlank(message = "Street is required")
        String street,
        @JsonProperty("houseNumber") String houseNumber,
        String apartment,
        @JsonProperty("postalCode") String postalCode,
        BigDecimal lat,
        BigDecimal lng
) {}
