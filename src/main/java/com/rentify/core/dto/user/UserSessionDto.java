package com.rentify.core.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Minimal authenticated user session payload")
public record UserSessionDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "First name", example = "Illia")
        @JsonProperty("firstName") String firstName,
        @Schema(description = "Last name", example = "Koval")
        @JsonProperty("lastName") String lastName,
        @Schema(description = "Avatar url", example = "https://example.com/resource.jpg")
        @JsonProperty("avatarUrl") String avatarUrl,
        @Schema(description = "Roles", example = "[\"ROLE_USER\"]")
        @JsonProperty("roles") Set<String> roles
) {}
