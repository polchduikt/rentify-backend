package com.rentify.core.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Delete account request payload")
public record DeleteAccountRequestDto(
        @JsonProperty("currentPassword")
        String currentPassword
) {
}
