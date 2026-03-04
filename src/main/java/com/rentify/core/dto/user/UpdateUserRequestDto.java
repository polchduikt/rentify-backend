package com.rentify.core.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDto(
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        @JsonProperty("firstName")
        String firstName,

        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        @JsonProperty("lastName")
        String lastName,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
        @JsonProperty("phone")
        String phone
) {}
