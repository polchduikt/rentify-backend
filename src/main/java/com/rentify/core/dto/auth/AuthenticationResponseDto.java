package com.rentify.core.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response payload")
public record AuthenticationResponseDto(
        @JsonProperty("token")
        @Schema(description = "Token (empty in cookie auth mode)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", nullable = true)
        String token
) {}
