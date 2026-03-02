package com.rentify.core.dto;

public record RegisterRequestDto(
        String firstName,
        String lastName,
        String email,
        String password
) {}