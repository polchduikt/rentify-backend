package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.AmenityCategory;
import com.rentify.core.enums.PropertyMarketType;
import com.rentify.core.enums.RentalType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PropertySearchCriteriaDto(
        @JsonProperty("propertyId") Long propertyId,
        @JsonProperty("cityId") Long cityId,
        @JsonProperty("districtId") Long districtId,
        @JsonProperty("metroStationId") Long metroStationId,
        @JsonProperty("residentialComplexId") Long residentialComplexId,
        String country,
        String region,
        String city,
        @JsonProperty("lat") Double lat,
        @JsonProperty("lng") Double lng,
        @JsonProperty("radiusKm") Double radiusKm,
        @JsonProperty("southWestLat") Double southWestLat,
        @JsonProperty("southWestLng") Double southWestLng,
        @JsonProperty("northEastLat") Double northEastLat,
        @JsonProperty("northEastLng") Double northEastLng,
        @JsonProperty("minPrice") BigDecimal minPrice,
        @JsonProperty("maxPrice") BigDecimal maxPrice,
        @JsonProperty("minRooms") Short minRooms,
        @JsonProperty("maxRooms") Short maxRooms,
        @JsonProperty("minFloor") Short minFloor,
        @JsonProperty("maxFloor") Short maxFloor,
        @JsonProperty("minTotalFloors") Short minTotalFloors,
        @JsonProperty("maxTotalFloors") Short maxTotalFloors,
        @JsonProperty("minSleepingPlaces") Short minSleepingPlaces,
        @JsonProperty("maxSleepingPlaces") Short maxSleepingPlaces,
        @JsonProperty("minArea") Double minArea,
        @JsonProperty("maxArea") Double maxArea,
        @JsonProperty("dateFrom") LocalDate dateFrom,
        @JsonProperty("dateTo") LocalDate dateTo,
        @JsonProperty("rentalType") RentalType rentalType,
        @JsonProperty("marketType") PropertyMarketType marketType,
        @JsonProperty("propertyType") String propertyType,
        @JsonProperty("verifiedProperty") Boolean verifiedProperty,
        @JsonProperty("verifiedRealtor") Boolean verifiedRealtor,
        @JsonProperty("hideDuplicates") Boolean hideDuplicates,
        @JsonProperty("amenityIds") List<Long> amenityIds,
        @JsonProperty("amenitySlugs") List<String> amenitySlugs,
        @JsonProperty("amenityCategories") List<AmenityCategory> amenityCategories,
        @JsonProperty("petsAllowed") Boolean petsAllowed
) {}
