package com.rentify.core.service.impl;

import com.rentify.core.dto.user.ChangePasswordRequestDto;
import com.rentify.core.dto.user.PublicUserProfileDto;
import com.rentify.core.dto.user.UpdateUserRequestDto;
import com.rentify.core.dto.user.UserResponseDto;
import com.rentify.core.entity.User;
import com.rentify.core.enums.SubscriptionPlan;
import com.rentify.core.mapper.UserMapper;
import com.rentify.core.repository.UserRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.CloudinaryService;
import com.rentify.core.service.UserService;
import com.rentify.core.validation.UserValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUserProfile() {
        User currentUser = authenticationService.getCurrentUser();
        return userMapper.toDto(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicUserProfileDto getPublicProfile(Long userId) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return userMapper.toPublicProfileDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateProfile(UpdateUserRequestDto request) {
        userValidator.validateUpdateProfile(request);
        User user = authenticationService.getCurrentUser();
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequestDto request) {
        userValidator.validateChangePassword(request);
        User user = authenticationService.getCurrentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteCurrentAccount(String currentPassword) {
        User user = authenticationService.getCurrentUser();
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalStateException("Account is already deactivated");
        }
        validateDeletePassword(user, currentPassword);

        user.setIsActive(false);
        user.setEmail(buildDeletedEmail(user.getId()));
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setFirstName(null);
        user.setLastName(null);
        user.setPhone(null);
        user.setAvatarUrl(null);
        user.setOauthProvider(null);
        user.setOauthSubject(null);
        user.setBalance(BigDecimal.ZERO);
        user.setSubscriptionPlan(SubscriptionPlan.FREE);
        user.setSubscriptionActiveUntil(null);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public String uploadAvatar(MultipartFile file) {
        User user = authenticationService.getCurrentUser();
        String previousAvatarUrl = user.getAvatarUrl();
        String imageUrl = cloudinaryService.uploadFile(file);
        deleteOldAvatarIfNeeded(previousAvatarUrl, imageUrl);
        user.setAvatarUrl(imageUrl);
        userRepository.save(user);
        return imageUrl;
    }

    @Override
    @Transactional
    public void deleteAvatar() {
        User user = authenticationService.getCurrentUser();
        if (user.getAvatarUrl() != null) {
            cloudinaryService.deleteFile(user.getAvatarUrl());
            user.setAvatarUrl(null);
            userRepository.save(user);
        }
    }

    private String buildDeletedEmail(Long userId) {
        long epochSeconds = Instant.now().getEpochSecond();
        return "deleted_" + userId + "_" + epochSeconds + "@deleted.local";
    }

    private void validateDeletePassword(User user, String currentPassword) {
        if (user.getOauthProvider() != null && !user.getOauthProvider().isBlank()) {
            return;
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password is required to delete account");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
    }

    private void deleteOldAvatarIfNeeded(String previousAvatarUrl, String newAvatarUrl) {
        if (previousAvatarUrl == null || previousAvatarUrl.isBlank()) {
            return;
        }
        if (previousAvatarUrl.equals(newAvatarUrl)) {
            return;
        }
        if (!isCloudinaryUrl(previousAvatarUrl)) {
            return;
        }
        cloudinaryService.deleteFile(previousAvatarUrl);
    }

    private boolean isCloudinaryUrl(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getHost() != null && uri.getHost().contains("res.cloudinary.com");
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
