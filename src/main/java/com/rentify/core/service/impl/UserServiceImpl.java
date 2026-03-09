package com.rentify.core.service.impl;

import com.rentify.core.dto.user.ChangePasswordRequestDto;
import com.rentify.core.dto.user.DeleteAccountRequestDto;
import com.rentify.core.dto.user.UpdateUserRequestDto;
import com.rentify.core.dto.user.UserResponseDto;
import com.rentify.core.entity.User;
import com.rentify.core.mapper.UserMapper;
import com.rentify.core.repository.UserRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.CloudinaryService;
import com.rentify.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUserProfile() {
        User currentUser = authenticationService.getCurrentUser();
        return userMapper.toDto(currentUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateProfile(UpdateUserRequestDto request) {
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
        User user = authenticationService.getCurrentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteCurrentAccount(DeleteAccountRequestDto request) {
        User user = authenticationService.getCurrentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalStateException("Account is already deactivated");
        }

        user.setIsActive(false);
        user.setEmail(buildDeletedEmail(user.getId()));
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setFirstName(null);
        user.setLastName(null);
        user.setPhone(null);
        user.setAvatarUrl(null);
        user.setOauthProvider(null);
        user.setOauthSubject(null);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public String uploadAvatar(MultipartFile file) {
        User user = authenticationService.getCurrentUser();
        String imageUrl = cloudinaryService.uploadFile(file);
        user.setAvatarUrl(imageUrl);
        userRepository.save(user);
        return imageUrl;
    }

    private String buildDeletedEmail(Long userId) {
        long epochSeconds = Instant.now().getEpochSecond();
        return "deleted_" + userId + "_" + epochSeconds + "@deleted.local";
    }
}
