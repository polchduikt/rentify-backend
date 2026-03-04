package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.RentalType;
import java.math.BigDecimal;
import java.util.List;

public record PropertySearchCriteriaDto(
        String city,
        @JsonProperty("minPrice") BigDecimal minPrice,
        @JsonProperty("maxPrice") BigDecimal maxPrice,
        @JsonProperty("minRooms") Short minRooms,
        @JsonProperty("maxRooms") Short maxRooms,
        @JsonProperty("minFloor") Short minFloor,
        @JsonProperty("maxFloor") Short maxFloor,
        @JsonProperty("minArea") Double minArea,
        @JsonProperty("maxArea") Double maxArea,
        @JsonProperty("rentalType") RentalType rentalType,
        @JsonProperty("amenityIds") List<Long> amenityIds,
        @JsonProperty("petsAllowed") Boolean petsAllowed
) {}
