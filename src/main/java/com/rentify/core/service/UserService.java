package com.rentify.core.service;

import com.rentify.core.dto.user.ChangePasswordRequestDto;
import com.rentify.core.dto.user.DeleteAccountRequestDto;
import com.rentify.core.dto.user.UpdateUserRequestDto;
import com.rentify.core.dto.user.UserResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponseDto getCurrentUserProfile();
    UserResponseDto updateProfile(UpdateUserRequestDto request);
    void changePassword(ChangePasswordRequestDto request);
    void deleteCurrentAccount(DeleteAccountRequestDto request);
    String uploadAvatar(MultipartFile file);
}
