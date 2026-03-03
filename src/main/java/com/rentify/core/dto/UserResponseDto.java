package com.rentify.core.dto;

import java.time.ZonedDateTime;
import java.util.Set;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String avatarUrl,
        Set<String> roles,
        ZonedDateTime createdAt
) {}