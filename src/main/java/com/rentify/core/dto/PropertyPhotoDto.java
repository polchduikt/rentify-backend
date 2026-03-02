package com.rentify.core.dto;

public record PropertyPhotoDto(
        Long id,
        String url,
        Integer sortOrder
) {}
