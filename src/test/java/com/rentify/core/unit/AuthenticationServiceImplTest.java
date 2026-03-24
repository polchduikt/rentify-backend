package com.rentify.core.unit;

import com.rentify.core.dto.auth.AuthenticationRequestDto;
import com.rentify.core.dto.auth.AuthenticationResponseDto;
import com.rentify.core.dto.auth.GoogleOAuthRequestDto;
import com.rentify.core.dto.auth.RegisterRequestDto;
import com.rentify.core.entity.Role;
import com.rentify.core.entity.User;
import com.rentify.core.exception.AccountDeactivatedException;
import com.rentify.core.exception.InvalidGoogleTokenException;
import com.rentify.core.exception.OAuthAccountLinkedToAnotherProviderException;
import com.rentify.core.repository.RoleRepository;
import com.rentify.core.repository.UserRepository;
import com.rentify.core.security.JwtService;
import com.rentify.core.security.SecurityUser;
import com.rentify.core.service.impl.AuthenticationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RoleRepository roleRepository;
    @Mock private JwtDecoder googleJwtDecoder;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private Role userRole;
    private User user;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().name("ROLE_USER").build();
        user = User.builder()
                .id(1L)
                .email("user@rentify.com")
                .password("encoded-pass")
                .firstName("Illia")
                .lastName("Koval")
                .isActive(true)
                .roles(Set.of(userRole))
                .build();

        ReflectionTestUtils.setField(authenticationService, "googleClientId", "test-google-client-id");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        void shouldRegisterUserAndReturnJwt_whenEmailIsFree() {
            RegisterRequestDto request = new RegisterRequestDto("Illia", "Koval", "+380991112233", "user@rentify.com", "StrongPass123!");
            when(userRepository.existsByEmail("user@rentify.com")).thenReturn(false);
            when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
            when(passwordEncoder.encode("StrongPass123!")).thenReturn("encoded-pass");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtService.generateToken(any(SecurityUser.class))).thenReturn("jwt-token");

            AuthenticationResponseDto result = authenticationService.register(request);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getEmail()).isEqualTo("user@rentify.com");
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-pass");
            assertThat(userCaptor.getValue().getRoles()).contains(userRole);
            assertThat(result.token()).isEqualTo("jwt-token");
        }

        @Test
        void shouldThrowIllegalArgument_whenEmailAlreadyTaken() {
            RegisterRequestDto request = new RegisterRequestDto("Illia", "Koval", "+380991112233", "user@rentify.com", "StrongPass123!");
            when(userRepository.existsByEmail("user@rentify.com")).thenReturn(true);

            assertThatThrownBy(() -> authenticationService.register(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already taken");
        }

        @Test
        void shouldThrowRuntimeException_whenRoleUserMissing() {
            RegisterRequestDto request = new RegisterRequestDto("Illia", "Koval", "+380991112233", "user@rentify.com", "StrongPass123!");
            when(userRepository.existsByEmail("user@rentify.com")).thenReturn(false);
            when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.register(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Error: ROLE_USER not found.");
        }
    }

    @Nested
    @DisplayName("authenticate()")
    class AuthenticateTests {

        @Test
        void shouldAuthenticateAndReturnJwt() {
            AuthenticationRequestDto request = new AuthenticationRequestDto("user@rentify.com", "StrongPass123!");
            Authentication authResult = new UsernamePasswordAuthenticationToken("user@rentify.com", "StrongPass123!");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authResult);
            when(userRepository.findByEmail("user@rentify.com")).thenReturn(Optional.of(user));
            when(jwtService.generateToken(any(SecurityUser.class))).thenReturn("jwt-token");

            AuthenticationResponseDto result = authenticationService.authenticate(request);

            assertThat(result.token()).isEqualTo("jwt-token");
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        void shouldThrowEntityNotFound_whenUserMissingAfterAuthentication() {
            AuthenticationRequestDto request = new AuthenticationRequestDto("missing@rentify.com", "StrongPass123!");
            Authentication authResult = new UsernamePasswordAuthenticationToken("missing@rentify.com", "StrongPass123!");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authResult);
            when(userRepository.findByEmail("missing@rentify.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.authenticate(request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("authenticateWithGoogle()")
    class AuthenticateWithGoogleTests {

        @Test
        void shouldThrowIllegalState_whenGoogleOAuthIsNotConfigured() {
            ReflectionTestUtils.setField(authenticationService, "googleClientId", "");
            GoogleOAuthRequestDto request = new GoogleOAuthRequestDto("id-token");

            assertThatThrownBy(() -> authenticationService.authenticateWithGoogle(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Google OAuth is not configured on server");
        }

        @Test
        void shouldThrowInvalidGoogleToken_whenDecoderFails() {
            GoogleOAuthRequestDto request = new GoogleOAuthRequestDto("bad-token");
            when(googleJwtDecoder.decode("bad-token")).thenThrow(new JwtException("invalid"));

            assertThatThrownBy(() -> authenticationService.authenticateWithGoogle(request))
                    .isInstanceOf(InvalidGoogleTokenException.class)
                    .hasMessage("Google token is invalid or expired");
        }

        @Test
        void shouldThrowInvalidGoogleToken_whenClaimsAreInvalid() {
            GoogleOAuthRequestDto request = new GoogleOAuthRequestDto("id-token");
            Jwt jwt = googleJwt("sub-1", "user@rentify.com", false, "Illia", "Koval", "https://avatar");
            when(googleJwtDecoder.decode("id-token")).thenReturn(jwt);

            assertThatThrownBy(() -> authenticationService.authenticateWithGoogle(request))
                    .isInstanceOf(InvalidGoogleTokenException.class)
                    .hasMessage("Google account email is not verified");
        }

        @Test
        void shouldAuthenticateExistingGoogleUser_whenAccountActive() {
            GoogleOAuthRequestDto request = new GoogleOAuthRequestDto("id-token");
            user.setOauthProvider("GOOGLE");
            user.setOauthSubject("sub-1");
            Jwt jwt = googleJwt("sub-1", "user@rentify.com", true, "Illia", "Koval", "https://avatar");
            when(googleJwtDecoder.decode("id-token")).thenReturn(jwt);
            when(userRepository.findByOauthProviderAndOauthSubject("GOOGLE", "sub-1")).thenReturn(Optional.of(user));
            when(jwtService.generateToken(any(SecurityUser.class))).thenReturn("jwt-token");

            AuthenticationResponseDto result = authenticationService.authenticateWithGoogle(request);

            assertThat(result.token()).isEqualTo("jwt-token");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void shouldThrowAccountDeactivated_whenGoogleUserInactive() {
            GoogleOAuthRequestDto request = new GoogleOAuthRequestDto("id-token");
            user.setOauthProvider("GOOGLE");
            user.setOauthSubject("sub-1");
            user.setIsActive(false);
            Jwt jwt = googleJwt("sub-1", "user@rentify.com", true, "Illia", "Koval", "https://avatar");
            when(googleJwtDecoder.decode("id-token")).thenReturn(jwt);
            when(userRepository.findByOauthProviderAndOauthSubject("GOOGLE", "sub-1")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> authenticationService.authenticateWithGoogle(request))
                    .isInstanceOf(AccountDeactivatedException.class)
                    .hasMessage("Account is deactivated");
        }

        @Test
        void shouldLinkGoogleAccountToExistingEmailUser_whenNoOauthLinked() {
            GoogleOAuthRequestDto request = new GoogleOAuthRequestDto("id-token");
            Jwt jwt = googleJwt("sub-1", "user@rentify.com", true, "NewFirst", "NewLast", "https://new-avatar");

            User localUser = User.builder()
                    .id(5L)
                    .email("user@rentify.com")
                    .password("encoded")
                    .isActive(true)
                    .roles(Set.of(userRole))
                    .build();

            when(googleJwtDecoder.decode("id-token")).thenReturn(jwt);
            when(userRepository.findByOauthProviderAndOauthSubject("GOOGLE", "sub-1")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("user@rentify.com")).thenReturn(Optional.of(localUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtService.generateToken(any(SecurityUser.class))).thenReturn("jwt-token");

            AuthenticationResponseDto result = authenticationService.authenticateWithGoogle(request);

            assertThat(result.token()).isEqualTo("jwt-token");
            assertThat(localUser.getOauthProvider()).isEqualTo("GOOGLE");
            assertThat(localUser.getOauthSubject()).isEqualTo("sub-1");
            assertThat(localUser.getFirstName()).isEqualTo("NewFirst");
            assertThat(localUser.getLastName()).isEqualTo("NewLast");
            assertThat(localUser.getAvatarUrl()).isEqualTo("https://new-avatar");
        }

        @Test
        void shouldThrowWhenExistingAccountLinkedToAnotherProvider() {
            GoogleOAuthRequestDto request = new GoogleOAuthRequestDto("id-token");
            Jwt jwt = googleJwt("sub-1", "user@rentify.com", true, "NewFirst", "NewLast", "https://new-avatar");
            User localUser = User.builder()
                    .email("user@rentify.com")
                    .oauthProvider("FACEBOOK")
                    .oauthSubject("fb-sub")
                    .isActive(true)
                    .roles(Set.of(userRole))
                    .build();

            when(googleJwtDecoder.decode("id-token")).thenReturn(jwt);
            when(userRepository.findByOauthProviderAndOauthSubject("GOOGLE", "sub-1")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("user@rentify.com")).thenReturn(Optional.of(localUser));

            assertThatThrownBy(() -> authenticationService.authenticateWithGoogle(request))
                    .isInstanceOf(OAuthAccountLinkedToAnotherProviderException.class)
                    .hasMessage("Account linked to another provider");
        }

        @Test
        void shouldCreateGoogleUser_whenNoMatchingAccountsFound() {
            GoogleOAuthRequestDto request = new GoogleOAuthRequestDto("id-token");
            Jwt jwt = googleJwt("sub-1", "new@rentify.com", true, "Illia", "Koval", "https://new-avatar");
            when(googleJwtDecoder.decode("id-token")).thenReturn(jwt);
            when(userRepository.findByOauthProviderAndOauthSubject("GOOGLE", "sub-1")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("new@rentify.com")).thenReturn(Optional.empty());
            when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
            when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-random");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtService.generateToken(any(SecurityUser.class))).thenReturn("jwt-token");

            AuthenticationResponseDto result = authenticationService.authenticateWithGoogle(request);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).save(userCaptor.capture());
            assertThat(result.token()).isEqualTo("jwt-token");
            assertThat(userCaptor.getValue().getEmail()).isEqualTo("new@rentify.com");
            assertThat(userCaptor.getValue().getOauthProvider()).isEqualTo("GOOGLE");
            assertThat(userCaptor.getValue().getOauthSubject()).isEqualTo("sub-1");
            assertThat(userCaptor.getValue().getIsActive()).isTrue();
            assertThat(userCaptor.getValue().getRoles()).contains(userRole);
        }
    }

    @Nested
    @DisplayName("getCurrentUser()")
    class GetCurrentUserTests {

        @Test
        void shouldReturnCurrentUserFromSecurityContext() {
            SecurityUser securityUser = new SecurityUser(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    securityUser, null, securityUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User result = authenticationService.getCurrentUser();

            assertThat(result).isEqualTo(user);
        }

        @Test
        void shouldThrowRuntimeException_whenAuthenticationMissing() {
            SecurityContextHolder.clearContext();

            assertThatThrownBy(() -> authenticationService.getCurrentUser())
                    .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                    .hasMessage("User not authenticated");
        }

        @Test
        void shouldThrowRuntimeException_whenAuthenticationNotAuthenticated() {
            Authentication authentication = new UsernamePasswordAuthenticationToken("principal", "creds");
            authentication.setAuthenticated(false);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            assertThatThrownBy(() -> authenticationService.getCurrentUser())
                    .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                    .hasMessage("User not authenticated");
        }
    }

    private Jwt googleJwt(
            String subject,
            String email,
            Boolean emailVerified,
            String givenName,
            String familyName,
            String picture
    ) {
        return new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of(
                        "sub", subject,
                        "email", email,
                        "email_verified", emailVerified,
                        "given_name", givenName,
                        "family_name", familyName,
                        "picture", picture
                )
        );
    }
}
