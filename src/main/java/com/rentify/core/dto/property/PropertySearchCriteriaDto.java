package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.AmenityCategory;
import com.rentify.core.enums.PropertyMarketType;
import com.rentify.core.enums.RentalType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Property search filters. All fields are optional and passed as query parameters.")
public record PropertySearchCriteriaDto(
        @Schema(description = "Exact property ID", example = "42")
        @JsonProperty("propertyId") Long propertyId,
        @Schema(description = "City ID", example = "1")
        @JsonProperty("cityId") Long cityId,
        @Schema(description = "District ID", example = "10")
        @JsonProperty("districtId") Long districtId,
        @Schema(description = "Metro station ID", example = "25")
        @JsonProperty("metroStationId") Long metroStationId,
        @Schema(description = "Residential complex ID", example = "7")
        @JsonProperty("residentialComplexId") Long residentialComplexId,
        @Schema(description = "Country name", example = "Ukraine")
        String country,
        @Schema(description = "Region name", example = "Kyivska")
        String region,
        @Schema(description = "City name", example = "Kyiv")
        String city,
        @Schema(description = "Latitude for radius search", example = "50.4501")
        @JsonProperty("lat") Double lat,
        @Schema(description = "Longitude for radius search", example = "30.5234")
        @JsonProperty("lng") Double lng,
        @Schema(description = "Radius in kilometers for lat/lng search", example = "3.0")
        @JsonProperty("radiusKm") Double radiusKm,
        @Schema(description = "Map viewport south-west latitude", example = "50.4000")
        @JsonProperty("southWestLat") Double southWestLat,
        @Schema(description = "Map viewport south-west longitude", example = "30.4500")
        @JsonProperty("southWestLng") Double southWestLng,
        @Schema(description = "Map viewport north-east latitude", example = "50.5000")
        @JsonProperty("northEastLat") Double northEastLat,
        @Schema(description = "Map viewport north-east longitude", example = "30.6000")
        @JsonProperty("northEastLng") Double northEastLng,
        @Schema(description = "Minimum price", example = "5000")
        @JsonProperty("minPrice") BigDecimal minPrice,
        @Schema(description = "Maximum price", example = "20000")
        @JsonProperty("maxPrice") BigDecimal maxPrice,
        @Schema(description = "Minimum rooms", example = "1")
        @JsonProperty("minRooms") Short minRooms,
        @Schema(description = "Maximum rooms", example = "3")
        @JsonProperty("maxRooms") Short maxRooms,
        @Schema(description = "Minimum floor", example = "1")
        @JsonProperty("minFloor") Short minFloor,
        @Schema(description = "Maximum floor", example = "10")
        @JsonProperty("maxFloor") Short maxFloor,
        @Schema(description = "Minimum total floors in building", example = "5")
        @JsonProperty("minTotalFloors") Short minTotalFloors,
        @Schema(description = "Maximum total floors in building", example = "25")
        @JsonProperty("maxTotalFloors") Short maxTotalFloors,
        @Schema(description = "Minimum sleeping places", example = "2")
        @JsonProperty("minSleepingPlaces") Short minSleepingPlaces,
        @Schema(description = "Maximum sleeping places", example = "6")
        @JsonProperty("maxSleepingPlaces") Short maxSleepingPlaces,
        @Schema(description = "Minimum area in square meters", example = "30")
        @JsonProperty("minArea") Double minArea,
        @Schema(description = "Maximum area in square meters", example = "120")
        @JsonProperty("maxArea") Double maxArea,
        @Schema(description = "Check-in date (inclusive)", example = "2026-03-20", format = "date")
        @JsonProperty("dateFrom") LocalDate dateFrom,
        @Schema(description = "Check-out date (inclusive)", example = "2026-03-25", format = "date")
        @JsonProperty("dateTo") LocalDate dateTo,
        @Schema(description = "Rental type", example = "SHORT_TERM")
        @JsonProperty("rentalType") RentalType rentalType,
        @Schema(description = "Market type", example = "SECONDARY")
        @JsonProperty("marketType") PropertyMarketType marketType,
        @Schema(description = "Property type slug/name", example = "apartment")
        @JsonProperty("propertyType") String propertyType,
        @Schema(description = "Filter verified property listings", example = "true")
        @JsonProperty("verifiedProperty") Boolean verifiedProperty,
        @Schema(description = "Filter verified realtor listings", example = "true")
        @JsonProperty("verifiedRealtor") Boolean verifiedRealtor,
        @Schema(description = "Hide duplicate listings", example = "true")
        @JsonProperty("hideDuplicates") Boolean hideDuplicates,
        @Schema(description = "Amenity IDs filter. Repeat parameter or pass comma-separated values.", example = "1,2,3")
        @JsonProperty("amenityIds") List<Long> amenityIds,
        @Schema(description = "Amenity slug filter. Repeat parameter or pass comma-separated values.", example = "wifi,parking")
        @JsonProperty("amenitySlugs") List<String> amenitySlugs,
        @Schema(description = "Amenity category filter. Repeat parameter or pass comma-separated values.", example = "BASIC")
        @JsonProperty("amenityCategories") List<AmenityCategory> amenityCategories,
        @Schema(description = "Pets allowed filter", example = "true")
        @JsonProperty("petsAllowed") Boolean petsAllowed
) {}
