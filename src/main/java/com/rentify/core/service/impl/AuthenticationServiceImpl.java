package com.rentify.core.service.impl;

import com.rentify.core.dto.auth.AuthenticationRequestDto;
import com.rentify.core.dto.auth.AuthenticationResponseDto;
import com.rentify.core.dto.auth.GoogleOAuthRequestDto;
import com.rentify.core.dto.auth.RegisterRequestDto;
import com.rentify.core.entity.Role;
import com.rentify.core.entity.User;
import com.rentify.core.exception.AccountDeactivatedException;
import com.rentify.core.exception.InvalidGoogleTokenException;
import com.rentify.core.exception.OAuthAccountLinkedToAnotherProviderException;
import com.rentify.core.repository.RoleRepository;
import com.rentify.core.repository.UserRepository;
import com.rentify.core.security.JwtService;
import com.rentify.core.security.SecurityUser;
import com.rentify.core.service.AuthenticationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String GOOGLE_PROVIDER = "GOOGLE";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    @Qualifier("googleJwtDecoder")
    private final JwtDecoder googleJwtDecoder;
    private final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Value("${application.security.oauth.google.client-id:}")
    private String googleClientId;

    @Override
    @Transactional
    public AuthenticationResponseDto register(RegisterRequestDto request) {
        logger.info("Registering new user with email: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already taken");
        }
        Role userRole = getUserRole();
        var roles = new HashSet<Role>();
        roles.add(userRole);
        var user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(roles)
                .isActive(true)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(new SecurityUser(user));
        return new AuthenticationResponseDto(jwtToken);
    }

    @Override
    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request) {
        logger.info("Authenticating user: {}", request.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        var jwtToken = jwtService.generateToken(new SecurityUser(user));
        return new AuthenticationResponseDto(jwtToken);
    }

    @Override
    @Transactional
    public AuthenticationResponseDto authenticateWithGoogle(GoogleOAuthRequestDto request) {
        logger.info("Authenticating via Google OAuth");
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new IllegalStateException("Google OAuth is not configured on server");
        }

        GoogleUserInfo googleUser = decodeGoogleIdToken(request.idToken());
        User user = findOrCreateGoogleUser(googleUser);

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AccountDeactivatedException("Account is deactivated");
        }

        String jwtToken = jwtService.generateToken(new SecurityUser(user));
        return new AuthenticationResponseDto(jwtToken);
    }

    @Override
    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        return securityUser.getUser();
    }

    private GoogleUserInfo decodeGoogleIdToken(String idToken) {
        Jwt jwt;
        try {
            jwt = googleJwtDecoder.decode(idToken);
        } catch (JwtException ex) {
            throw new InvalidGoogleTokenException("Invalid Google token");
        }

        String subject = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");

        if (subject == null || subject.isBlank()) {
            throw new InvalidGoogleTokenException("Invalid Google token");
        }
        if (email == null || email.isBlank()) {
            throw new InvalidGoogleTokenException("Invalid Google token");
        }
        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new InvalidGoogleTokenException("Invalid Google token");
        }

        return new GoogleUserInfo(
                subject,
                email.trim().toLowerCase(),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name"),
                jwt.getClaimAsString("picture")
        );
    }

    private User findOrCreateGoogleUser(GoogleUserInfo googleUser) {
        return userRepository.findByOauthProviderAndOauthSubject(GOOGLE_PROVIDER, googleUser.subject())
                .orElseGet(() -> userRepository.findByEmail(googleUser.email())
                        .map(existing -> userRepository.save(linkGoogleAccount(existing, googleUser)))
                        .orElseGet(() -> userRepository.save(createGoogleUser(googleUser))));
    }

    private User linkGoogleAccount(User user, GoogleUserInfo googleUser) {
        if (user.getOauthProvider() != null
                && (!GOOGLE_PROVIDER.equals(user.getOauthProvider())
                || (user.getOauthSubject() != null && !user.getOauthSubject().equals(googleUser.subject())))) {
            throw new OAuthAccountLinkedToAnotherProviderException("Account linked to another provider");
        }

        user.setOauthProvider(GOOGLE_PROVIDER);
        user.setOauthSubject(googleUser.subject());
        updateUserProfileFromGoogle(user, googleUser);
        ensureUserRolePresent(user);
        return user;
    }

    private User createGoogleUser(GoogleUserInfo googleUser) {
        Role userRole = getUserRole();
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .email(googleUser.email())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .oauthProvider(GOOGLE_PROVIDER)
                .oauthSubject(googleUser.subject())
                .firstName(googleUser.givenName())
                .lastName(googleUser.familyName())
                .avatarUrl(googleUser.pictureUrl())
                .isActive(true)
                .roles(roles)
                .build();
        return user;
    }

    private void updateUserProfileFromGoogle(User user, GoogleUserInfo googleUser) {
        if (googleUser.givenName() != null && !googleUser.givenName().isBlank()) {
            user.setFirstName(googleUser.givenName());
        }
        if (googleUser.familyName() != null && !googleUser.familyName().isBlank()) {
            user.setLastName(googleUser.familyName());
        }
        if (googleUser.pictureUrl() != null && !googleUser.pictureUrl().isBlank()) {
            user.setAvatarUrl(googleUser.pictureUrl());
        }
    }

    private void ensureUserRolePresent(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            roles.add(getUserRole());
            user.setRoles(roles);
        }
    }

    private Role getUserRole() {
        return roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: ROLE_USER not found."));
    }

    private record GoogleUserInfo(
            String subject,
            String email,
            String givenName,
            String familyName,
            String pictureUrl
    ) {
    }
}
