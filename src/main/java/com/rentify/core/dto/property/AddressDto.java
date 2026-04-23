package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Address payload")
public record AddressDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "Country", example = "Ukraine")
        String country,
        @Schema(description = "Region", example = "Kyivska oblast")
        String region,
        @Schema(description = "City name", example = "Kyiv")
        String city,
        @Schema(description = "City id", example = "42")
        @JsonProperty("cityId") Long cityId,
        @Schema(description = "District id", example = "42")
        @JsonProperty("districtId") Long districtId,
        @Schema(description = "Metro station id", example = "42")
        @JsonProperty("metroStationId") Long metroStationId,
        @Schema(description = "Residential complex id", example = "42")
        @JsonProperty("residentialComplexId") Long residentialComplexId,
        @Schema(description = "District name", example = "Sample value")
        @JsonProperty("districtName") String districtName,
        @Schema(description = "Metro station name", example = "Sample value")
        @JsonProperty("metroStationName") String metroStationName,
        @Schema(description = "Residential complex name", example = "Sample value")
        @JsonProperty("residentialComplexName") String residentialComplexName,
        @NotBlank(message = "Street is required")
        @Schema(description = "Street", example = "Sample value")
        String street,
        @Schema(description = "House number", example = "Sample value")
        @JsonProperty("houseNumber") String houseNumber,
        @Schema(description = "Apartment", example = "Sample value")
        String apartment,
        @Schema(description = "Postal code", example = "02000")
        @JsonProperty("postalCode") String postalCode,
        @Schema(description = "Lat", example = "100.0")
        BigDecimal lat,
        @Schema(description = "Lng", example = "100.0")
        BigDecimal lng
) {}
