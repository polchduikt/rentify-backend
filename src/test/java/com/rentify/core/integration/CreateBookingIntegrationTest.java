package com.rentify.core.integration;

import com.rentify.core.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.time.LocalDate;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Create booking integration tests")
class CreateBookingIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Positive: tenant creates booking for active short-term listing")
    void shouldCreateBookingSuccessfully() throws Exception {
        String hostToken = registerUserAndGetToken(randomEmail("booking-host"), "StrongPass123!", "Booking", "Host");
        String tenantEmail = randomEmail("booking-tenant");
        String tenantToken = registerUserAndGetToken(tenantEmail, "StrongPass123!", "Booking", "Tenant");

        long propertyId = createActiveShortTermProperty(hostToken, "Bookable listing", "Kyiv");
        LocalDate from = LocalDate.now().plusDays(7);
        LocalDate to = from.plusDays(3);

        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", bearerToken(tenantToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingPayload(propertyId, from, to, (short) 2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.propertyId").value(propertyId))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalPrice").isNumber());
    }

    @Test
    @DisplayName("Negative: host cannot book own listing")
    void shouldRejectBookingOwnProperty() throws Exception {
        String hostToken = registerUserAndGetToken(randomEmail("own-host"), "StrongPass123!", "Own", "Host");
        long propertyId = createActiveShortTermProperty(hostToken, "Own listing", "Kyiv");
        LocalDate from = LocalDate.now().plusDays(5);
        LocalDate to = from.plusDays(2);

        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", bearerToken(hostToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingPayload(propertyId, from, to, (short) 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("own property")));
    }

    @Test
    @DisplayName("Negative: overlapping dates return conflict")
    void shouldRejectOverlappingBooking() throws Exception {
        String hostToken = registerUserAndGetToken(randomEmail("overlap-host"), "StrongPass123!", "Overlap", "Host");
        String tenant1Token = registerUserAndGetToken(randomEmail("overlap-tenant1"), "StrongPass123!", "Overlap", "Tenant1");
        String tenant2Token = registerUserAndGetToken(randomEmail("overlap-tenant2"), "StrongPass123!", "Overlap", "Tenant2");
        long propertyId = createActiveShortTermProperty(hostToken, "Overlap listing", "Kyiv");
        LocalDate from = LocalDate.now().plusDays(10);
        LocalDate to = from.plusDays(4);

        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", bearerToken(tenant1Token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingPayload(propertyId, from, to, (short) 2))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", bearerToken(tenant2Token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingPayload(propertyId, from, to, (short) 2))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already booked")));
    }
}
