package com.rentify.core.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequestDto(
        @NotBlank(message = "Current password is required")
        @JsonProperty("currentPassword")
        String currentPassword
) {
}
