package com.rentify.core.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record GoogleOAuthRequestDto(
        @NotBlank(message = "Google idToken is required")
        @JsonProperty("id_token")
        @JsonAlias("idToken")
        String idToken
) {
}
