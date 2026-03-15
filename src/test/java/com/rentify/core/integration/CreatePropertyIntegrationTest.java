package com.rentify.core.integration;

import com.rentify.core.entity.Property;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Create property integration tests")
class CreatePropertyIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Positive: authorized user creates listing and becomes host")
    void shouldCreatePropertyForAuthorizedUser() throws Exception {
        String email = randomEmail("host");
        String token = registerUserAndGetToken(email, "StrongPass123!", "Host", "User");
        Long hostId = userRepository.findByEmail(email).orElseThrow().getId();

        MvcResult result = mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortTermPropertyPayload("US03 listing", "Kyiv"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.hostId").value(hostId))
                .andReturn();

        long propertyId = readJson(result).get("id").asLong();
        Property saved = propertyRepository.findById(propertyId).orElseThrow();
        assertThat(saved.getHost().getId()).isEqualTo(hostId);
        assertThat(saved.getStatus()).isEqualTo(PropertyStatus.DRAFT);
    }

    @Test
    @DisplayName("Negative: create listing without token is forbidden")
    void shouldRejectCreatePropertyWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortTermPropertyPayload("No token listing", "Kyiv"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Negative: invalid listing payload returns 400")
    void shouldReturnBadRequestForInvalidListingPayload() throws Exception {
        String token = registerUserAndGetToken(randomEmail("host-invalid"), "StrongPass123!", "Host", "Invalid");
        Map<String, Object> payload = shortTermPropertyPayload("Invalid listing", "Kyiv");
        payload.remove("rentalType");

        mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("rentalType")));
    }
}
