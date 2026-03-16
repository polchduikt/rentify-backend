package com.rentify.core.service;

import com.rentify.core.dto.auth.AuthenticationRequestDto;
import com.rentify.core.dto.auth.AuthenticationResponseDto;
import com.rentify.core.dto.auth.GoogleOAuthRequestDto;
import com.rentify.core.dto.auth.RegisterRequestDto;
import com.rentify.core.entity.User;

public interface AuthenticationService {
    AuthenticationResponseDto register(RegisterRequestDto request);
    AuthenticationResponseDto authenticate(AuthenticationRequestDto request);
    AuthenticationResponseDto authenticateWithGoogle(GoogleOAuthRequestDto request);
    User getCurrentUser();
}