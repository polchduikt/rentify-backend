package com.rentify.core.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.Set;

public record UserResponseDto(
        Long id,
        @JsonProperty("firstName") String firstName,
        @JsonProperty("lastName") String lastName,
        @JsonProperty("email") String email,
        @JsonProperty("phone") String phone,
        @JsonProperty("avatarUrl") String avatarUrl,
        @JsonProperty("isActive") Boolean isActive,
        @JsonProperty("roles") Set<String> roles,
        @JsonProperty("createdAt") ZonedDateTime createdAt,
        @JsonProperty("updatedAt") ZonedDateTime updatedAt
) {}
