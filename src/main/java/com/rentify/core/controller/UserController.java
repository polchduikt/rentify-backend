package com.rentify.core.controller;

import com.rentify.core.dto.user.ChangePasswordRequestDto;
import com.rentify.core.dto.user.DeleteAccountRequestDto;
import com.rentify.core.dto.user.PublicUserProfileDto;
import com.rentify.core.dto.user.UpdateUserRequestDto;
import com.rentify.core.dto.user.UserResponseDto;
import com.rentify.core.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Users", description = "User profile and account management endpoints")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Returns full profile information for the authenticated user."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Profile retrieved",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
    )
    public ResponseEntity<UserResponseDto> getMyProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get public profile by user id",
            description = "Returns public profile data that can be displayed in listing/review cards."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Public profile retrieved",
                    content = @Content(schema = @Schema(implementation = PublicUserProfileDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<PublicUserProfileDto> getPublicProfile(
            @Parameter(description = "User ID", example = "17")
            @PathVariable @Positive Long userId) {
        return ResponseEntity.ok(userService.getPublicProfile(userId));
    }

    @PutMapping("/me")
    @Operation(
            summary = "Update current user profile",
            description = "Updates editable profile fields for the authenticated user."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Profile updated",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))
    )
    public ResponseEntity<UserResponseDto> updateProfile(
            @Valid @RequestBody UpdateUserRequestDto request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @PatchMapping("/me/password")
    @Operation(
            summary = "Change current user password",
            description = "Changes account password after validating current password and strength rules."
    )
    @ApiResponse(responseCode = "204", description = "Password changed")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto request) {
        userService.changePassword(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @Operation(
            summary = "Deactivate current user account",
            description = "Deactivates account, logs user out effectively by invalidating account activity status."
    )
    @ApiResponse(responseCode = "204", description = "Account deactivated")
    public ResponseEntity<Void> deleteProfile(
            @RequestBody(required = false) DeleteAccountRequestDto request) {
        String currentPassword = request != null ? request.currentPassword() : null;
        userService.deleteCurrentAccount(currentPassword);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload user avatar",
            description = "Uploads and sets new avatar image for the authenticated user profile."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Avatar uploaded",
            content = @Content(schema = @Schema(implementation = String.class))
    )
    public ResponseEntity<String> uploadAvatar(
            @Parameter(description = "Image file to upload")
            @RequestPart("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(file);
        return ResponseEntity.ok(avatarUrl);
    }

    @DeleteMapping("/me/avatar")
    @Operation(
            summary = "Delete current user avatar",
            description = "Removes current avatar and resets user profile image to empty state."
    )
    @ApiResponse(responseCode = "204", description = "Avatar deleted")
    public ResponseEntity<Void> deleteAvatar() {
        userService.deleteAvatar();
        return ResponseEntity.noContent().build();
    }
}
