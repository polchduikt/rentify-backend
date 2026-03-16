package com.rentify.core.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Change password request payload")
public record ChangePasswordRequestDto(
        @NotBlank(message = "Current password is required")
        @JsonProperty("currentPassword")
        @Schema(description = "Current password", example = "StrongPass123!")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 128, message = "New password must be between 8 and 128 characters")
        @JsonProperty("newPassword")
        @Schema(description = "New password", example = "StrongPass123!")
        String newPassword,

        @NotBlank(message = "Password confirmation is required")
        @JsonProperty("confirmPassword")
        String confirmPassword
) {
}
