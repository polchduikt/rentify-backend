package com.rentify.core.dto;

import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.RentalType;
import java.math.BigDecimal;
import java.util.List;

public record PropertySearchCriteriaDto(
        String city,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Short minRooms,
        Short maxRooms,
        Short minFloor,
        Short maxFloor,
        Double minArea,
        Double maxArea,
        RentalType rentalType,
        List<Long> amenityIds,
        Boolean petsAllowed
) {}