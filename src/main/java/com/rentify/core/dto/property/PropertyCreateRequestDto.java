package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.PropertyMarketType;
import com.rentify.core.enums.RentalType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record PropertyCreateRequestDto(
        @Valid
        @NotNull(message = "Address is required")
        @JsonProperty("address")
        AddressDto address,

        @NotBlank(message = "Title is required")
        @Size(max = 160, message = "Title is too long")
        @JsonProperty("title")
        String title,

        @JsonProperty("description")
        String description,

        @NotNull(message = "Rental type is required")
        @JsonProperty("rentalType")
        RentalType rentalType,

        @NotBlank(message = "Property type is required")
        @JsonProperty("propertyType")
        String propertyType,

        @JsonProperty("marketType")
        PropertyMarketType marketType,

        @JsonProperty("isVerifiedProperty")
        Boolean isVerifiedProperty,

        @JsonProperty("isVerifiedRealtor")
        Boolean isVerifiedRealtor,

        @JsonProperty("isDuplicate")
        Boolean isDuplicate,

        @Min(value = 1, message = "Rooms must be at least 1")
        @JsonProperty("rooms")
        Short rooms,

        @JsonProperty("floor")
        Short floor,

        @JsonProperty("totalFloors")
        Short totalFloors,

        @JsonProperty("areaSqm")
        BigDecimal areaSqm,

        @Min(value = 1, message = "Max guests must be at least 1")
        @JsonProperty("maxGuests")
        Short maxGuests,

        @JsonProperty("checkInTime")
        LocalTime checkInTime,

        @JsonProperty("checkOutTime")
        LocalTime checkOutTime,

        @JsonProperty("amenityIds")
        List<Long> amenityIds,

        @JsonProperty("amenitySlugs")
        List<String> amenitySlugs,

        @Valid
        @JsonProperty("pricing")
        PropertyPricingDto pricing,

        @Valid
        @JsonProperty("rules")
        PropertyRuleDto rules
) {}
