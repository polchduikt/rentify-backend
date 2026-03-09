package com.rentify.core.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(
        @NotBlank(message = "Current password is required")
        @JsonProperty("currentPassword")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 128, message = "New password must be between 8 and 128 characters")
        @JsonProperty("newPassword")
        String newPassword,

        @NotBlank(message = "Password confirmation is required")
        @JsonProperty("confirmPassword")
        String confirmPassword
) {
}
