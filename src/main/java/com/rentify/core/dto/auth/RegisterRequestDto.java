package com.rentify.core.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Register request payload")
public record RegisterRequestDto(
        @NotBlank
        @Size(min = 2, max = 50)
        @JsonProperty("firstName")
        @Schema(description = "First name", example = "Illia")
        String firstName,

        @NotBlank
        @Size(min = 2, max = 50)
        @JsonProperty("lastName")
        @Schema(description = "Last name", example = "Koval")
        String lastName,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
        @JsonProperty("phone")
        @Schema(description = "Phone", example = "+380991112233")
        String phone,

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
