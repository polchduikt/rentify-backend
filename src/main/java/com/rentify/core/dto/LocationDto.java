package com.rentify.core.dto;

public record LocationDto(
        Long id,
        String country,
        String region,
        String city
) {}