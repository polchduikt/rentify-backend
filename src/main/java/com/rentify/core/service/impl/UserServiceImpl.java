package com.rentify.core.service.impl;

import com.rentify.core.dto.user.UpdateUserRequestDto;
import com.rentify.core.dto.user.UserResponseDto;
import com.rentify.core.entity.User;
import com.rentify.core.mapper.UserMapper;
import com.rentify.core.repository.UserRepository;
import com.rentify.core.service.AuthenticationService;
import com.rentify.core.service.CloudinaryService;
import com.rentify.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

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
    public String uploadAvatar(MultipartFile file) {
        User user = authenticationService.getCurrentUser();
        String imageUrl = cloudinaryService.uploadFile(file);
        user.setAvatarUrl(imageUrl);
        userRepository.save(user);
        return imageUrl;
    }
}
