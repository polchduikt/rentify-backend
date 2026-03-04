package com.rentify.core.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank
        @Size(min = 2, max = 50)
        @JsonProperty("firstName")
        String firstName,

        @NotBlank
        @Size(min = 2, max = 50)
        @JsonProperty("lastName")
        String lastName,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
        @JsonProperty("phone")
        String phone,

        @NotBlank
        @Email
        @JsonProperty("email")
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        @JsonProperty("password")
        String password
) {}
