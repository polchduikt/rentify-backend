package com.rentify.core.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequestDto(
        @Size(min = 2, max = 50, message = "Ім'я має бути від 2 до 50 символів")
        String firstName,

        @Size(min = 2, max = 50, message = "Прізвище має бути від 2 до 50 символів")
        String lastName,

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Невірний формат номера телефону")
        String phone
) {}