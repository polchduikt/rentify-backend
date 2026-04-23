package com.rentify.core.service;

import com.rentify.core.dto.auth.AuthenticationRequestDto;
import com.rentify.core.dto.auth.AuthenticationResponseDto;
import com.rentify.core.dto.auth.GoogleOAuthRequestDto;
import com.rentify.core.dto.auth.RegisterRequestDto;
import com.rentify.core.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    AuthenticationResponseDto register(RegisterRequestDto request);
    AuthenticationResponseDto authenticate(AuthenticationRequestDto request);
    AuthenticationResponseDto authenticateWithGoogle(GoogleOAuthRequestDto request);
    void logout(HttpServletRequest request, HttpServletResponse response);
    User getCurrentUser();
}