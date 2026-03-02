package com.rentify.core.dto;

public record ReviewRequestDto(
        Long propertyId,
        Short rating,
        String comment
) {}