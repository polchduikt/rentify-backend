package com.rentify.core.integration;

import com.rentify.core.entity.Role;
import com.rentify.core.entity.User;
import com.rentify.core.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Registration integration tests")
class RegistrationIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Positive: user can register and gets JWT + ROLE_USER + hashed password")
    void shouldRegisterUserSuccessfully() throws Exception {
        String email = randomEmail("register");
        String rawPassword = "StrongPass123!";

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload(email, rawPassword, "Illia", "Koval"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String token = extractToken(result);
        assertThat(token).isNotBlank();

        User saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(saved.getRoles()).extracting(Role::getName).contains("ROLE_USER");
        assertThat(saved.getPassword()).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Negative: duplicate email is rejected")
    void shouldRejectDuplicateEmail() throws Exception {
        String email = randomEmail("duplicate");
        String rawPassword = "StrongPass123!";
        registerUserAndGetToken(email, rawPassword, "First", "User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload(email, rawPassword, "Second", "User"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Email already taken")));
    }

    @Test
    @DisplayName("Negative: invalid payload returns validation error")
    void shouldReturnBadRequestForInvalidRegistrationPayload() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>(registerPayload(randomEmail("invalid"), "StrongPass123!", "", "User"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("firstName")));
    }

    @Test
    @DisplayName("Negative: email without @ is rejected")
    void shouldRejectEmailWithoutAtSign() throws Exception {
        Map<String, Object> payload = registerPayload("invalid-email.example.com", "StrongPass123!", "Valid", "User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("email")));
    }

    @Test
    @DisplayName("Negative: firstName with one char is rejected")
    void shouldRejectFirstNameWithOneChar() throws Exception {
        Map<String, Object> payload = registerPayload(randomEmail("short-name"), "StrongPass123!", "I", "User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("firstName")));
    }

    @Test
    @DisplayName("Negative: password shorter than 8 chars is rejected")
    void shouldRejectPasswordWithout8Chars() throws Exception {
        Map<String, Object> payload = registerPayload(randomEmail("short-pass"), "Short1!", "Valid", "User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("password")));
    }
}
