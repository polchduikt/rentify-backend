package com.rentify.core.dto;

import java.time.ZonedDateTime;
import java.util.Set;

public record UserDto(
        Long id,
        String email,
        String fullName,
        String phone,
        String avatarUrl,
        Set<String> roles,
        ZonedDateTime createdAt
) {}