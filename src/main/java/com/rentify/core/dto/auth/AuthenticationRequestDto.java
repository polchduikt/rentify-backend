package com.rentify.core.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication request payload")
public record AuthenticationRequestDto(
        @NotBlank
        @Email
        @JsonProperty("email")
        @Schema(description = "Email", example = "user@example.com")
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        @JsonProperty("password")
        @Schema(description = "Password", example = "StrongPass123!")
        String password
) {}
