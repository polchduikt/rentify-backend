package com.rentify.core.service;

import com.rentify.core.dto.user.ChangePasswordRequestDto;
import com.rentify.core.dto.user.PublicUserProfileDto;
import com.rentify.core.dto.user.UpdateUserRequestDto;
import com.rentify.core.dto.user.UserResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponseDto getCurrentUserProfile();
    PublicUserProfileDto getPublicProfile(Long userId);
    UserResponseDto updateProfile(UpdateUserRequestDto request);
    void changePassword(ChangePasswordRequestDto request);
    void deleteCurrentAccount(String currentPassword);
    String uploadAvatar(MultipartFile file);
    void deleteAvatar();
}
