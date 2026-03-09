package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record AddressDto(
        Long id,
        @Valid
        LocationDto location,
        @JsonProperty("cityId") Long cityId,
        @JsonProperty("districtId") Long districtId,
        @JsonProperty("metroStationId") Long metroStationId,
        @JsonProperty("residentialComplexId") Long residentialComplexId,
        @JsonProperty("districtName") String districtName,
        @JsonProperty("metroStationName") String metroStationName,
        @JsonProperty("residentialComplexName") String residentialComplexName,
        @NotBlank(message = "Street is required")
        String street,
        @JsonProperty("houseNumber") String houseNumber,
        String apartment,
        @JsonProperty("postalCode") String postalCode,
        BigDecimal lat,
        BigDecimal lng
) {}
