package com.rentify.core.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticationRequestDto(
        @NotBlank
        @Email
        @JsonProperty("email")
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        @JsonProperty("password")
        String password
) {}
