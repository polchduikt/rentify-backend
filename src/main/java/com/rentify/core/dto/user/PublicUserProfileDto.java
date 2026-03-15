package com.rentify.core.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public record PublicUserProfileDto(
        Long id,
        @JsonProperty("firstName") String firstName,
        @JsonProperty("lastName") String lastName,
        @JsonProperty("phone") String phone,
        @JsonProperty("avatarUrl") String avatarUrl,
        @JsonProperty("createdAt") ZonedDateTime createdAt
) {
}
