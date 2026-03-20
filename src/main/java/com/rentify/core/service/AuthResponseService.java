package com.rentify.core.service;

import com.rentify.core.dto.auth.AuthenticationResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface AuthResponseService {

    ResponseEntity<AuthenticationResponseDto> buildAuthResponse(
            AuthenticationResponseDto authResponse,
            HttpServletResponse response,
            HttpStatus status
    );
}
