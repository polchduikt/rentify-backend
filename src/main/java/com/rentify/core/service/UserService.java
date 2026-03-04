package com.rentify.core.service;

import com.rentify.core.dto.user.UpdateUserRequestDto;
import com.rentify.core.dto.user.UserResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponseDto getCurrentUserProfile();
    UserResponseDto updateProfile(UpdateUserRequestDto request);
    String uploadAvatar(MultipartFile file);
}