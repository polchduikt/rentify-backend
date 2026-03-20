package com.rentify.core.controller;

import com.rentify.core.config.AuthCookieService;
import com.rentify.core.dto.auth.AuthenticationRequestDto;
import com.rentify.core.dto.auth.AuthenticationResponseDto;
import com.rentify.core.dto.auth.GoogleOAuthRequestDto;
import com.rentify.core.dto.auth.RegisterRequestDto;
import com.rentify.core.service.AuthResponseService;
import com.rentify.core.service.AuthenticationService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration and login endpoints")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final AuthCookieService authCookieService;
    private final AuthResponseService authResponseService;

    @PostMapping("/register")
    @Operation(
            summary = "Register user",
            description = "Registers a new account and returns JWT tokens for immediate authorized usage."
    )
    @ApiResponse(
            responseCode = "201",
            description = "User registered and authenticated",
            content = @Content(schema = @Schema(implementation = AuthenticationResponseDto.class))
    )
    public ResponseEntity<AuthenticationResponseDto> register(
            @Valid @RequestBody RegisterRequestDto request,
            HttpServletResponse response
    ) {
        AuthenticationResponseDto authResponse = authenticationService.register(request);
        return authResponseService.buildAuthResponse(authResponse, response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login with email and password",
            description = "Authenticates user credentials and returns an access token for protected endpoints."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Authentication succeeded",
            content = @Content(schema = @Schema(implementation = AuthenticationResponseDto.class))
    )
    public ResponseEntity<AuthenticationResponseDto> authenticate(
            @Valid @RequestBody AuthenticationRequestDto request,
            HttpServletResponse response
    ) {
        AuthenticationResponseDto authResponse = authenticationService.authenticate(request);
        return authResponseService.buildAuthResponse(authResponse, response, HttpStatus.OK);
    }

    @PostMapping("/google")
    @Operation(
            summary = "Login with Google OAuth token",
            description = "Authenticates user using Google ID token and returns Rentify access token."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Google authentication succeeded",
            content = @Content(schema = @Schema(implementation = AuthenticationResponseDto.class))
    )
    public ResponseEntity<AuthenticationResponseDto> authenticateWithGoogle(
            @Valid @RequestBody GoogleOAuthRequestDto request,
            HttpServletResponse response
    ) {
        AuthenticationResponseDto authResponse = authenticationService.authenticateWithGoogle(request);
        return authResponseService.buildAuthResponse(authResponse, response, HttpStatus.OK);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout from current browser session",
            description = "Clears authentication cookie in cookie strategy mode."
    )
    @ApiResponse(responseCode = "204", description = "Session cookie cleared")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authCookieService.clearAccessTokenCookie(response);
        return ResponseEntity.noContent().build();
    }
}
