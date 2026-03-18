package com.rentify.core.integration;

import com.rentify.core.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Cookie auth integration tests")
@TestPropertySource(properties = {
        "application.security.auth.strategy=cookie",
        "application.security.auth.cookie-secure=false",
        "application.security.auth.csrf.cookie-secure=false"
})
class CookieAuthenticationIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Positive: registration sets auth cookie and allows profile access without Authorization header")
    void shouldAuthenticateWithHttpOnlyCookie() throws Exception {
        String email = randomEmail("cookie-auth");
        String rawPassword = "StrongPass123!";

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload(email, rawPassword, "Cookie", "User"))))
                .andExpect(status().isCreated())
                .andExpect(cookie().exists("rentify_access_token"))
                .andExpect(jsonPath("$.token").isEmpty())
                .andReturn();

        var authCookie = registerResult.getResponse().getCookie("rentify_access_token");
        assertThat(authCookie).isNotNull();
        assertThat(authCookie.isHttpOnly()).isTrue();

        mockMvc.perform(get("/api/v1/users/profile").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @DisplayName("Positive: logout clears auth cookie in cookie mode")
    void shouldClearCookieOnLogout() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("rentify_access_token", 0));
    }
}
