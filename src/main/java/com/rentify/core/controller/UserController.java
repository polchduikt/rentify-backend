package com.rentify.core.controller;

import com.rentify.core.dto.user.ChangePasswordRequestDto;
import com.rentify.core.dto.user.DeleteAccountRequestDto;
import com.rentify.core.dto.user.PublicUserProfileDto;
import com.rentify.core.dto.user.UpdateUserRequestDto;
import com.rentify.core.dto.user.UserResponseDto;
import com.rentify.core.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> getMyProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @GetMapping("/{userId}/public")
    public ResponseEntity<PublicUserProfileDto> getPublicProfile(@PathVariable @Positive Long userId) {
        return ResponseEntity.ok(userService.getPublicProfile(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponseDto> updateProfile(
            @Valid @RequestBody UpdateUserRequestDto request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @PatchMapping("/profile/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto request) {
        userService.changePassword(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteProfile(
            @Valid @RequestBody DeleteAccountRequestDto request) {
        userService.deleteCurrentAccount(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAvatar(@RequestPart("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(file);
        return ResponseEntity.ok(avatarUrl);
    }

    @DeleteMapping("/profile/avatar")
    public ResponseEntity<Void> deleteAvatar() {
        userService.deleteAvatar();
        return ResponseEntity.noContent().build();
    }
}
