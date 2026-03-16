package com.rentify.core.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Public user profile payload")
public record PublicUserProfileDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @JsonProperty("firstName") String firstName,
        @JsonProperty("lastName") String lastName,
        @JsonProperty("phone") String phone,
        @JsonProperty("avatarUrl") String avatarUrl,
        @JsonProperty("createdAt") ZonedDateTime createdAt
) {
}
