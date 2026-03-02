package com.rentify.core.service.impl;

import com.rentify.core.dto.AuthenticationRequestDto;
import com.rentify.core.dto.AuthenticationResponseDto;
import com.rentify.core.dto.RegisterRequestDto;
import com.rentify.core.entity.User;
import com.rentify.core.repository.RoleRepository;
import com.rentify.core.repository.UserRepository;
import com.rentify.core.security.JwtService;
import com.rentify.core.security.SecurityUser;
import com.rentify.core.service.AuthenticationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Override
    @Transactional
    public AuthenticationResponseDto register(RegisterRequestDto request) {
        logger.info("Registering new user with email: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already taken");
        }
        var userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: ROLE_USER not found."));
        var user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(java.util.Set.of(userRole))
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
    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        return securityUser.getUser();
    }
}