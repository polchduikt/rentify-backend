package com.rentify.core.service;

import com.rentify.core.dto.AuthenticationRequestDto;
import com.rentify.core.dto.AuthenticationResponseDto;
import com.rentify.core.dto.RegisterRequestDto;
import com.rentify.core.entity.User;

public interface AuthenticationService {
    AuthenticationResponseDto register(RegisterRequestDto request);
    AuthenticationResponseDto authenticate(AuthenticationRequestDto request);
    User getCurrentUser();
}