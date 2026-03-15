package com.rentify.core.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeleteAccountRequestDto(
        @JsonProperty("currentPassword")
        String currentPassword
) {
}
