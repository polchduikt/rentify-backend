package com.rentify.core.unit;

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
import com.rentify.core.service.impl.UserServiceImpl;
import com.rentify.core.validation.UserValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private AuthenticationService authenticationService;
    @Mock private UserMapper userMapper;
    @Mock private UserRepository userRepository;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserValidator userValidator;
    @Mock private MultipartFile multipartFile;

    @InjectMocks
    private UserServiceImpl userService;

    private User currentUser;
    private UserResponseDto userResponseDto;
    private PublicUserProfileDto publicProfileDto;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L)
                .email("user@rentify.com")
                .password("hashed-password")
                .firstName("Illia")
                .lastName("Koval")
                .phone("+380991112233")
                .avatarUrl("https://res.cloudinary.com/demo/image/upload/v123/rentify/avatars/old-avatar.jpg")
                .isActive(true)
                .balance(BigDecimal.valueOf(123.45))
                .subscriptionPlan(SubscriptionPlan.PREMIUM)
                .subscriptionActiveUntil(ZonedDateTime.now().plusDays(30))
                .build();

        userResponseDto = new UserResponseDto(
                1L,
                "Illia",
                "Koval",
                "user@rentify.com",
                "+380991112233",
                "https://res.cloudinary.com/demo/image/upload/v123/rentify/avatars/old-avatar.jpg",
                true,
                BigDecimal.valueOf(123.45),
                SubscriptionPlan.PREMIUM,
                ZonedDateTime.now().plusDays(30),
                Set.of("ROLE_USER"),
                ZonedDateTime.now(),
                ZonedDateTime.now()
        );

        publicProfileDto = new PublicUserProfileDto(
                1L,
                "Illia",
                "Koval",
                "+380991112233",
                "https://res.cloudinary.com/demo/image/upload/v123/rentify/avatars/old-avatar.jpg",
                ZonedDateTime.now()
        );
    }

    @Nested
    @DisplayName("getCurrentUserProfile()")
    class GetCurrentUserProfileTests {

        @Test
        void shouldReturnCurrentUserProfileDto() {
            when(authenticationService.getCurrentUser()).thenReturn(currentUser);
            when(userMapper.toDto(currentUser)).thenReturn(userResponseDto);

            UserResponseDto result = userService.getCurrentUserProfile();

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.email()).isEqualTo("user@rentify.com");
            verify(userMapper).toDto(currentUser);
        }
    }

    @Nested
    @DisplayName("getPublicProfile()")
    class GetPublicProfileTests {

        @Test
        void shouldReturnPublicProfile_whenUserExistsAndActive() {
            when(userRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(currentUser));
            when(userMapper.toPublicProfileDto(currentUser)).thenReturn(publicProfileDto);

            PublicUserProfileDto result = userService.getPublicProfile(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.firstName()).isEqualTo("Illia");
        }

        @Test
        void shouldThrowEntityNotFound_whenUserDoesNotExistOrInactive() {
            when(userRepository.findByIdAndIsActiveTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getPublicProfile(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("updateProfile()")
    class UpdateProfileTests {

        @Test
        void shouldUpdateOnlyProvidedFields() {
            UpdateUserRequestDto request = new UpdateUserRequestDto("NewFirst", null, "+380501234567");
            when(authenticationService.getCurrentUser()).thenReturn(currentUser);
            when(userRepository.save(currentUser)).thenReturn(currentUser);
            when(userMapper.toDto(currentUser)).thenReturn(userResponseDto);

            userService.updateProfile(request);

            verify(userValidator).validateUpdateProfile(request);
            assertThat(currentUser.getFirstName()).isEqualTo("NewFirst");
            assertThat(currentUser.getLastName()).isEqualTo("Koval");
            assertThat(currentUser.getPhone()).isEqualTo("+380501234567");
            verify(userRepository).save(currentUser);
        }
    }

    @Nested
    @DisplayName("changePassword()")
    class ChangePasswordTests {

        @Test
        void shouldChangePassword_whenCurrentPasswordIsCorrect() {
            ChangePasswordRequestDto request = new ChangePasswordRequestDto("old-pass", "new-pass", "new-pass");
            when(authenticationService.getCurrentUser()).thenReturn(currentUser);
            when(passwordEncoder.matches("old-pass", "hashed-password")).thenReturn(true);
            when(passwordEncoder.encode("new-pass")).thenReturn("new-hash");

            userService.changePassword(request);

            verify(userValidator).validateChangePassword(request);
            assertThat(currentUser.getPassword()).isEqualTo("new-hash");
            verify(userRepository).save(currentUser);
        }

        @Test
        void shouldThrowIllegalArgument_whenCurrentPasswordIsIncorrect() {
            ChangePasswordRequestDto request = new ChangePasswordRequestDto("wrong-pass", "new-pass", "new-pass");
            when(authenticationService.getCurrentUser()).thenReturn(currentUser);
            when(passwordEncoder.matches("wrong-pass", "hashed-password")).thenReturn(false);

            assertThatThrownBy(() -> userService.changePassword(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Current password is incorrect");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("deleteCurrentAccount()")
    class DeleteCurrentAccountTests {

        @Test
        void shouldAnonymizeAndDeactivateAccount_whenPasswordIsValid() {
            when(authenticationService.getCurrentUser()).thenReturn(currentUser);
            when(passwordEncoder.matches("old-pass", "hashed-password")).thenReturn(true);
            when(passwordEncoder.encode(anyString())).thenReturn("random-hash");

            userService.deleteCurrentAccount("old-pass");

            verifyNoInteractions(userValidator);
            assertThat(currentUser.getIsActive()).isFalse();
            assertThat(currentUser.getEmail()).startsWith("deleted_1_").endsWith("@deleted.local");
            assertThat(currentUser.getPassword()).isEqualTo("random-hash");
            assertThat(currentUser.getFirstName()).isNull();
            assertThat(currentUser.getLastName()).isNull();
            assertThat(currentUser.getPhone()).isNull();
            assertThat(currentUser.getAvatarUrl()).isNull();
            assertThat(currentUser.getOauthProvider()).isNull();
            assertThat(currentUser.getOauthSubject()).isNull();
            assertThat(currentUser.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(currentUser.getSubscriptionPlan()).isEqualTo(SubscriptionPlan.FREE);
            assertThat(currentUser.getSubscriptionActiveUntil()).isNull();
            verify(userRepository).save(currentUser);
        }

        @Test
        void shouldThrowIllegalArgument_whenPasswordMissingForLocalAccount() {
            when(authenticationService.getCurrentUser()).thenReturn(currentUser);

            assertThatThrownBy(() -> userService.deleteCurrentAccount(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Current password is required to delete account");
        }

        @Test
        void shouldSkipPasswordValidationForOauthAccount() {
            currentUser.setOauthProvider("GOOGLE");
            when(authenticationService.getCurrentUser()).thenReturn(currentUser);
            when(passwordEncoder.encode(anyString())).thenReturn("random-hash");

            userService.deleteCurrentAccount(null);

            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(userRepository).save(currentUser);
        }

        @Test
        void shouldThrowIllegalState_whenAccountAlreadyDeactivated() {
            currentUser.setIsActive(false);
            when(authenticationService.getCurrentUser()).thenReturn(currentUser);

            assertThatThrownBy(() -> userService.deleteCurrentAccount("any-pass"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Account is already deactivated");
        }
    }

    @Nested
    @DisplayName("uploadAvatar()")
    class UploadAvatarTests {

        @Test
        void shouldUploadAvatarDeletePreviousCloudinaryFileAndPersistUser() {
            when(authenticationService.getCurrentUser()).thenReturn(currentUser);
            when(cloudinaryService.uploadFile(multipartFile)).thenReturn("https://cdn/new-avatar.jpg");

            String result = userService.uploadAvatar(multipartFile);

            assertThat(result).isEqualTo("https://cdn/new-avatar.jpg");
            assertThat(currentUser.getAvatarUrl()).isEqualTo("https://cdn/new-avatar.jpg");
            verify(cloudinaryService).deleteFile("https://res.cloudinary.com/demo/image/upload/v123/rentify/avatars/old-avatar.jpg");
            verify(userRepository).save(currentUser);
        }
    }

    @Nested
    @DisplayName("deleteAvatar()")
    class DeleteAvatarTests {

        @Test
        void shouldDeleteAvatarAndPersist_whenAvatarExists() {
            when(authenticationService.getCurrentUser()).thenReturn(currentUser);

            userService.deleteAvatar();

            verify(cloudinaryService).deleteFile("https://res.cloudinary.com/demo/image/upload/v123/rentify/avatars/old-avatar.jpg");
            assertThat(currentUser.getAvatarUrl()).isNull();
            verify(userRepository).save(currentUser);
        }

        @Test
        void shouldDoNothing_whenAvatarIsMissing() {
            currentUser.setAvatarUrl(null);
            when(authenticationService.getCurrentUser()).thenReturn(currentUser);

            userService.deleteAvatar();

            verify(cloudinaryService, never()).deleteFile(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }
}
