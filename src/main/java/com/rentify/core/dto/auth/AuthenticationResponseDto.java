package com.rentify.core.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthenticationResponseDto(
        @JsonProperty("token")
        String token
) {}
