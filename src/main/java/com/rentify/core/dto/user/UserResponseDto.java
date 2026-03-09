package com.rentify.core.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rentify.core.enums.SubscriptionPlan;

import java.math.BigDecimal;
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
        @JsonProperty("balance") BigDecimal balance,
        @JsonProperty("subscriptionPlan") SubscriptionPlan subscriptionPlan,
        @JsonProperty("subscriptionActiveUntil") ZonedDateTime subscriptionActiveUntil,
        @JsonProperty("roles") Set<String> roles,
        @JsonProperty("createdAt") ZonedDateTime createdAt,
        @JsonProperty("updatedAt") ZonedDateTime updatedAt
) {}
