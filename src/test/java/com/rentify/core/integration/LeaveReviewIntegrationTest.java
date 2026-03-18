package com.rentify.core.integration;

import com.rentify.core.entity.Booking;
import com.rentify.core.enums.BookingStatus;
import com.rentify.core.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.time.LocalDate;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Leave review integration tests")
class LeaveReviewIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Positive: tenant can leave review after completed booking")
    void shouldCreateReviewAfterCompletedStay() throws Exception {
        String hostToken = registerUserAndGetToken(randomEmail("review-host"), "StrongPass123!", "Review", "Host");
        String tenantToken = registerUserAndGetToken(randomEmail("review-tenant"), "StrongPass123!", "Review", "Tenant");
        long propertyId = createActiveShortTermProperty(hostToken, "Review listing", "Kyiv");

        long bookingId = createBookingAndReturnId(tenantToken, propertyId, LocalDate.now().plusDays(15), LocalDate.now().plusDays(18), (short) 2);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        mockMvc.perform(post("/api/v1/reviews")
                        .header("Authorization", bearerToken(tenantToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewPayload(propertyId, bookingId, (short) 5, "Great stay!"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.propertyId").value(propertyId))
                .andExpect(jsonPath("$.bookingId").value(bookingId))
                .andExpect(jsonPath("$.rating").value(5));

        mockMvc.perform(get("/api/v1/reviews/property/{propertyId}", propertyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("Negative: duplicate review for same booking is rejected")
    void shouldRejectDuplicateReviewForBooking() throws Exception {
        String hostToken = registerUserAndGetToken(randomEmail("dup-review-host"), "StrongPass123!", "Dup", "Host");
        String tenantToken = registerUserAndGetToken(randomEmail("dup-review-tenant"), "StrongPass123!", "Dup", "Tenant");
        long propertyId = createActiveShortTermProperty(hostToken, "Dup review listing", "Kyiv");

        long bookingId = createBookingAndReturnId(tenantToken, propertyId, LocalDate.now().plusDays(20), LocalDate.now().plusDays(22), (short) 1);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        mockMvc.perform(post("/api/v1/reviews")
                        .header("Authorization", bearerToken(tenantToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewPayload(propertyId, bookingId, (short) 5, "First review"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/reviews")
                        .header("Authorization", bearerToken(tenantToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewPayload(propertyId, bookingId, (short) 4, "Second review"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already reviewed")));
    }

    @Test
    @DisplayName("Negative: invalid rating returns 400")
    void shouldRejectInvalidRating() throws Exception {
        String hostToken = registerUserAndGetToken(randomEmail("rating-host"), "StrongPass123!", "Rating", "Host");
        String tenantToken = registerUserAndGetToken(randomEmail("rating-tenant"), "StrongPass123!", "Rating", "Tenant");
        long propertyId = createActiveShortTermProperty(hostToken, "Rating listing", "Kyiv");

        long bookingId = createBookingAndReturnId(tenantToken, propertyId, LocalDate.now().plusDays(25), LocalDate.now().plusDays(27), (short) 1);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        mockMvc.perform(post("/api/v1/reviews")
                        .header("Authorization", bearerToken(tenantToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewPayload(propertyId, bookingId, (short) 6, "Invalid rating"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Rating must be from 1 to 5")));
    }

    @Test
    @DisplayName("Negative: unauthenticated user cannot leave review")
    void shouldRejectReviewWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewPayload(1L, 1L, (short) 5, "No token"))))
                .andExpect(status().isForbidden());
    }
}
