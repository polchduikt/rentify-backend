package com.rentify.core.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Delete account request payload")
public record DeleteAccountRequestDto(
        @Schema(description = "Current account password (required for local auth users)")
        String currentPassword
) {
}
