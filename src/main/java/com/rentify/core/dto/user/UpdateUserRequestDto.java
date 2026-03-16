package com.rentify.core.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Update user request payload")
public record UpdateUserRequestDto(
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        @JsonProperty("firstName")
        @Schema(description = "First name", example = "Illia")
        String firstName,

        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        @JsonProperty("lastName")
        @Schema(description = "Last name", example = "Koval")
        String lastName,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
        @JsonProperty("phone")
        @Schema(description = "Phone", example = "+380991112233")
        String phone
) {}
