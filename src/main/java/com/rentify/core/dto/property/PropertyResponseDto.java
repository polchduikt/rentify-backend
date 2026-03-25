package com.rentify.core.dto.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.PropertyMarketType;
import com.rentify.core.enums.PropertyType;
import com.rentify.core.enums.RentalType;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Property response payload")
public record PropertyResponseDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "Host id", example = "42")
        @JsonProperty("hostId") Long hostId,
        @Schema(description = "Address", example = "Sample value")
        AddressDto address,
        @Schema(description = "Title", example = "Cozy apartment in Kyiv center")
        String title,
        @Schema(description = "Description", example = "Spacious apartment with balcony and modern renovation.")
        String description,
        @Schema(description = "Rental type", example = "SHORT_TERM")
        @JsonProperty("rentalType") RentalType rentalType,
        @Schema(description = "Status", example = "ACTIVE")
        PropertyStatus status,
        @Schema(description = "Property type", example = "apartment")
        @JsonProperty("propertyType") PropertyType propertyType,
        @Schema(description = "Market type", example = "SECONDARY")
        @JsonProperty("marketType") PropertyMarketType marketType,
        @Schema(description = "Is verified property", example = "true")
        @JsonProperty("isVerifiedProperty") Boolean isVerifiedProperty,
        @Schema(description = "Is verified realtor", example = "true")
        @JsonProperty("isVerifiedRealtor") Boolean isVerifiedRealtor,
        @Schema(description = "Is duplicate", example = "true")
        @JsonProperty("isDuplicate") Boolean isDuplicate,
        @Schema(description = "Is top promoted", example = "true")
        @JsonProperty("isTopPromoted") Boolean isTopPromoted,
        @Schema(description = "View count", example = "1")
        @JsonProperty("viewCount") Long viewCount,
        @Schema(description = "Review count", example = "1")
        @JsonProperty("reviewCount") Long reviewCount,
        @Schema(description = "Average rating", example = "100.0")
        @JsonProperty("averageRating") BigDecimal averageRating,
        @Schema(description = "Top promoted until", example = "2026-03-15T10:30:00+02:00")
        @JsonProperty("topPromotedUntil") ZonedDateTime topPromotedUntil,
        @Schema(description = "Rooms", example = "1")
        Short rooms,
        @Schema(description = "Floor", example = "1")
        Short floor,
        @Schema(description = "Total floors", example = "1")
        @JsonProperty("totalFloors") Short totalFloors,
        @Schema(description = "Area sqm", example = "65.5")
        @JsonProperty("areaSqm") BigDecimal areaSqm,
        @Schema(description = "Max guests", example = "1")
        @JsonProperty("maxGuests") Short maxGuests,
        @Schema(description = "Check in time", example = "14:00:00")
        @JsonProperty("checkInTime") LocalTime checkInTime,
        @Schema(description = "Check out time", example = "14:00:00")
        @JsonProperty("checkOutTime") LocalTime checkOutTime,
        @Schema(description = "Pricing", example = "Sample value")
        PropertyPricingDto pricing,
        @Schema(description = "Rules", example = "Sample value")
        PropertyRuleDto rules,
        @Schema(description = "Photos", example = "[...]")
        List<PropertyPhotoDto> photos,
        @Schema(description = "Amenities", example = "Sample value")
        Set<AmenityDto> amenities,
        @Schema(description = "Created at", example = "2026-03-15T10:30:00+02:00")
        @JsonProperty("createdAt") ZonedDateTime createdAt,
        @Schema(description = "Updated at", example = "2026-03-15T10:30:00+02:00")
        @JsonProperty("updatedAt") ZonedDateTime updatedAt
) {}
