package com.rentify.core.service.impl;

import com.rentify.core.config.AuthCookieService;
import com.rentify.core.dto.auth.AuthenticationResponseDto;
import com.rentify.core.mapper.AuthenticationMapper;
import com.rentify.core.service.AuthResponseService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthResponseServiceImpl implements AuthResponseService {

    private final AuthCookieService authCookieService;
    private final AuthenticationMapper authenticationMapper;

    @Override
    public ResponseEntity<AuthenticationResponseDto> buildAuthResponse(
            AuthenticationResponseDto authResponse,
            HttpServletResponse response,
            HttpStatus status
    ) {
        if (authCookieService.isCookieStrategyEnabled()) {
            authCookieService.writeAccessTokenCookie(response, authResponse.token());
            return ResponseEntity.status(status).body(authenticationMapper.toEmptyAuthenticationResponse());
        }

        return ResponseEntity.status(status).body(authResponse);
    }
}
