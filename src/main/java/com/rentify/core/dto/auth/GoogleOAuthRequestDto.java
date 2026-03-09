package com.rentify.core.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleOAuthRequestDto(
        @NotBlank(message = "Google idToken is required")
        String idToken
) {
}
