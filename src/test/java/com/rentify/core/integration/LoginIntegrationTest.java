package com.rentify.core.integration;

import com.rentify.core.entity.Role;
import com.rentify.core.entity.User;
import com.rentify.core.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Login integration tests")
class LoginIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Positive: registered user can log in and receives valid JWT subject")
    void shouldLoginSuccessfully() throws Exception {
        String email = randomEmail("login");
        String rawPassword = "StrongPass123!";
        registerUserAndGetToken(email, rawPassword, "Login", "User");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload(email, rawPassword))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String token = extractToken(result);
        assertThat(jwtService.extractUsername(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("Negative: wrong password returns 401")
    void shouldReturnUnauthorizedForWrongCredentials() throws Exception {
        String email = randomEmail("wrong-pass");
        registerUserAndGetToken(email, "StrongPass123!", "Wrong", "Pass");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload(email, "WrongPass999!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("Negative: deactivated account cannot log in")
    void shouldReturnForbiddenForDeactivatedAccount() throws Exception {
        String email = randomEmail("inactive");
        String rawPassword = "StrongPass123!";
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();

        User inactiveUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .firstName("Inactive")
                .lastName("User")
                .isActive(false)
                .roles(Set.of(userRole))
                .build();
        userRepository.save(inactiveUser);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload(email, rawPassword))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Account is deactivated"));
    }

    @Test
    @DisplayName("Negative: non-existent email returns 401")
    void shouldRejectNonExistentEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                loginPayload("nobody@example.com", "StrongPass123!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("Invalid email or password")));
    }
}
