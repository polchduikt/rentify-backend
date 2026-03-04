package com.rentify.core.dto.conversation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequestDto(
        @NotBlank(message = "Message text cannot be empty")
        @Size(max = 3000, message = "Message text is too long")
        @JsonProperty("text")
        String text
) {}
