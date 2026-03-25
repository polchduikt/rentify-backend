package com.rentify.core.integration;

import com.rentify.core.entity.Property;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.enums.PropertyType;
import com.rentify.core.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@DisplayName("Search properties integration tests")
class SearchPropertiesIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Positive: search returns only ACTIVE listings")
    void shouldReturnOnlyActiveListings() throws Exception {
        String token = registerUserAndGetToken(randomEmail("search-host"), "StrongPass123!", "Search", "Host");
        long activeId = createActiveShortTermProperty(token, "Active listing", "Kyiv");
        long inactiveId = createActiveShortTermProperty(token, "Inactive listing", "Kyiv");

        Property inactive = propertyRepository.findById(inactiveId).orElseThrow();
        inactive.setStatus(PropertyStatus.INACTIVE);
        propertyRepository.save(inactive);

        mockMvc.perform(get("/api/v1/properties")
                        .param("city", "Kyiv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value((int) activeId));
    }

    @Test
    @DisplayName("Positive: pagination params page/size/sort are applied")
    void shouldSupportPaginationParameters() throws Exception {
        String token = registerUserAndGetToken(randomEmail("search-page"), "StrongPass123!", "Search", "Page");
        createActiveShortTermProperty(token, "Listing A", "Kyiv");
        createActiveShortTermProperty(token, "Listing B", "Kyiv");

        mockMvc.perform(get("/api/v1/properties")
                        .param("city", "Kyiv")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("Positive: empty result returns HTTP 200 with empty content")
    void shouldReturnEmptyListWhenNothingFound() throws Exception {
        mockMvc.perform(get("/api/v1/properties")
                        .param("city", "NoSuchCity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("Negative: invalid date range filter returns 400")
    void shouldReturnBadRequestForInvalidSearchCriteria() throws Exception {
        mockMvc.perform(get("/api/v1/properties")
                        .param("dateFrom", "2026-04-10")
                        .param("dateTo", "2026-04-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("dateFrom")));
    }

    @Test
    @DisplayName("Positive: search filters by propertyType regardless of stored case")
    void shouldFilterByPropertyTypeIgnoringStoredCase() throws Exception {
        String token = registerUserAndGetToken(randomEmail("search-type"), "StrongPass123!", "Search", "Type");
        long apartmentId = createActiveShortTermProperty(token, "Apartment listing", "Kyiv");
        long houseId = createActiveShortTermProperty(token, "House listing", "Kyiv");

        Property house = propertyRepository.findById(houseId).orElseThrow();
        house.setPropertyType(PropertyType.HOUSE);
        propertyRepository.save(house);

        jdbcTemplate.update("UPDATE properties SET property_type = ? WHERE id = ?", "APARTMENT", apartmentId);

        mockMvc.perform(get("/api/v1/properties")
                        .param("city", "Kyiv")
                        .param("propertyType", "apartment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value((int) apartmentId));
    }

    @Test
    @DisplayName("Positive: minPrice uses monthly price for LONG_TERM search")
    void shouldFilterLongTermByMonthlyPrice() throws Exception {
        String token = registerUserAndGetToken(randomEmail("search-price"), "StrongPass123!", "Search", "Price");
        createActiveLongTermProperty(token, "Long-term 12k", "Kyiv", 12000.0);
        long expectedPropertyId = createActiveLongTermProperty(token, "Long-term 16k", "Kyiv", 16000.0);

        mockMvc.perform(get("/api/v1/properties")
                        .param("city", "Kyiv")
                        .param("rentalType", "LONG_TERM")
                        .param("minPrice", "16000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value((int) expectedPropertyId));
    }
}
