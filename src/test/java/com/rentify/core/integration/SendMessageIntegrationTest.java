package com.rentify.core.integration;

import com.rentify.core.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Send message integration tests")
class SendMessageIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Positive: tenant sends first message and can read thread")
    void shouldCreateConversationAndReturnMessage() throws Exception {
        String hostToken = registerUserAndGetToken(randomEmail("msg-host"), "StrongPass123!", "Message", "Host");
        String tenantToken = registerUserAndGetToken(randomEmail("msg-tenant"), "StrongPass123!", "Message", "Tenant");
        long propertyId = createActiveShortTermProperty(hostToken, "Message listing", "Kyiv");

        MvcResult sendResult = mockMvc.perform(post("/api/v1/conversations/property/{propertyId}", propertyId)
                        .header("Authorization", bearerToken(tenantToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messagePayload("Hello host!"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TEXT"))
                .andExpect(jsonPath("$.text").value("Hello host!"))
                .andReturn();

        long conversationId = readJson(sendResult).get("conversationId").asLong();
        assertThat(conversationRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/v1/conversations/{conversationId}/messages", conversationId)
                        .header("Authorization", bearerToken(tenantToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$[0].text").value("Hello host!"));
    }

    @Test
    @DisplayName("Negative: host cannot initiate conversation with own listing")
    void shouldRejectHostInitiatingOwnConversation() throws Exception {
        String hostToken = registerUserAndGetToken(randomEmail("self-msg-host"), "StrongPass123!", "Self", "Host");
        long propertyId = createActiveShortTermProperty(hostToken, "Self message listing", "Kyiv");

        mockMvc.perform(post("/api/v1/conversations/property/{propertyId}", propertyId)
                        .header("Authorization", bearerToken(hostToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messagePayload("Self message"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Host cannot initiate")));
    }

    @Test
    @DisplayName("Negative: unauthenticated user cannot send message")
    void shouldRejectMessageWithoutToken() throws Exception {
        String hostToken = registerUserAndGetToken(randomEmail("msg-noauth-host"), "StrongPass123!", "NoAuth", "Host");
        long propertyId = createActiveShortTermProperty(hostToken, "No auth listing", "Kyiv");

        mockMvc.perform(post("/api/v1/conversations/property/{propertyId}", propertyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messagePayload("Anonymous message"))))
                .andExpect(status().isForbidden());
    }
}
