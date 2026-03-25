package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.PropertyMarketType;
import com.rentify.core.enums.PropertyType;
import com.rentify.core.enums.RentalType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Property create request payload")
public record PropertyCreateRequestDto(
        @Valid
        @NotNull(message = "Address is required")
        @JsonProperty("address")
        @Schema(description = "Address")
        AddressDto address,

        @NotBlank(message = "Title is required")
        @Size(max = 160, message = "Title is too long")
        @JsonProperty("title")
        @Schema(description = "Title", example = "Cozy apartment in Kyiv center")
        String title,

        @JsonProperty("description")
        @Schema(description = "Description", example = "Spacious apartment with balcony and modern renovation.")
        String description,

        @NotNull(message = "Rental type is required")
        @JsonProperty("rentalType")
        @Schema(description = "Rental type", example = "SHORT_TERM")
        RentalType rentalType,

        @NotNull(message = "Property type is required")
        @JsonProperty("propertyType")
        @Schema(description = "Property type", example = "apartment (APARTMENT)")
        PropertyType propertyType,

        @JsonProperty("marketType")
        @Schema(description = "Market type", example = "SECONDARY")
        PropertyMarketType marketType,

        @JsonProperty("isVerifiedProperty")
        @Schema(description = "Is verified property", example = "true")
        Boolean isVerifiedProperty,

        @JsonProperty("isVerifiedRealtor")
        @Schema(description = "Is verified realtor", example = "true")
        Boolean isVerifiedRealtor,

        @JsonProperty("isDuplicate")
        @Schema(description = "Is duplicate", example = "true")
        Boolean isDuplicate,

        @Min(value = 1, message = "Rooms must be at least 1")
        @JsonProperty("rooms")
        @Schema(description = "Rooms", example = "1")
        Short rooms,

        @JsonProperty("floor")
        @Schema(description = "Floor", example = "1")
        Short floor,

        @JsonProperty("totalFloors")
        @Schema(description = "Total floors", example = "1")
        Short totalFloors,

        @JsonProperty("areaSqm")
        @Schema(description = "Area sqm", example = "65.5")
        BigDecimal areaSqm,

        @Min(value = 1, message = "Max guests must be at least 1")
        @JsonProperty("maxGuests")
        @Schema(description = "Max guests", example = "1")
        Short maxGuests,

        @JsonProperty("checkInTime")
        @Schema(description = "Check in time", example = "10:00:00")
        LocalTime checkInTime,

        @JsonProperty("checkOutTime")
        @Schema(description = "Check out time", example = "12:00:00")
        LocalTime checkOutTime,

        @JsonProperty("amenityIds")
        @Schema(description = "Amenity ids", example = "[1, 2]")
        List<Long> amenityIds,

        @JsonProperty("amenitySlugs")
        @Schema(description = "Amenity slugs", example = "[\"internet\", \"air_conditioning\"]")
        List<String> amenitySlugs,

        @Valid
        @JsonProperty("pricing")
        @Schema(description = "Pricing")
        PropertyPricingDto pricing,

        @Valid
        @JsonProperty("rules")
        @Schema(description = "Rules")
        PropertyRuleDto rules
) {}
