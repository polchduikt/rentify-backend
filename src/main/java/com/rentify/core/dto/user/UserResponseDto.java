package com.rentify.core.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.SubscriptionPlan;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User response payload")
public record UserResponseDto(
        @Schema(description = "Id", example = "42")
        Long id,
        @Schema(description = "First name", example = "Illia")
        @JsonProperty("firstName") String firstName,
        @Schema(description = "Last name", example = "Koval")
        @JsonProperty("lastName") String lastName,
        @Schema(description = "Email", example = "user@example.com")
        @JsonProperty("email") String email,
        @Schema(description = "Phone", example = "+380991112233")
        @JsonProperty("phone") String phone,
        @Schema(description = "Avatar url", example = "https://example.com/resource.jpg")
        @JsonProperty("avatarUrl") String avatarUrl,
        @Schema(description = "Is active", example = "true")
        @JsonProperty("isActive") Boolean isActive,
        @Schema(description = "Balance", example = "100.0")
        @JsonProperty("balance") BigDecimal balance,
        @Schema(description = "Subscription plan", example = "FREE")
        @JsonProperty("subscriptionPlan") SubscriptionPlan subscriptionPlan,
        @Schema(description = "Subscription active until", example = "2026-03-15T10:30:00+02:00")
        @JsonProperty("subscriptionActiveUntil") ZonedDateTime subscriptionActiveUntil,
        @Schema(description = "Roles", example = "Sample value")
        @JsonProperty("roles") Set<String> roles,
        @Schema(description = "Created at", example = "2026-03-15T10:30:00+02:00")
        @JsonProperty("createdAt") ZonedDateTime createdAt,
        @Schema(description = "Updated at", example = "2026-03-15T10:30:00+02:00")
        @JsonProperty("updatedAt") ZonedDateTime updatedAt
) {}
