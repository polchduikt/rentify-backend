package com.rentify.core.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequestDto(
        @NotBlank(message = "Message text cannot be empty")
        String text
) {}