package com.rentify.core.integration.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.core.entity.Property;
import com.rentify.core.entity.Role;
import com.rentify.core.enums.PropertyStatus;
import com.rentify.core.repository.BookingRepository;
import com.rentify.core.repository.ConversationRepository;
import com.rentify.core.repository.MessageRepository;
import com.rentify.core.repository.PropertyRepository;
import com.rentify.core.repository.ReviewRepository;
import com.rentify.core.repository.RoleRepository;
import com.rentify.core.repository.UserRepository;
import com.rentify.core.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> postgres = SharedPostgresContainer.getInstance();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    protected RoleRepository roleRepository;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected PropertyRepository propertyRepository;
    @Autowired
    protected BookingRepository bookingRepository;
    @Autowired
    protected ConversationRepository conversationRepository;
    @Autowired
    protected MessageRepository messageRepository;
    @Autowired
    protected ReviewRepository reviewRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected JwtService jwtService;

    @BeforeEach
    void resetDatabaseAndSeedRoles() {
        truncateAllTables();

        ensureRoleExists("ROLE_USER");
        ensureRoleExists("ROLE_ADMIN");
    }

    private void truncateAllTables() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'public'",
                String.class
        );
        if (tables.isEmpty()) {
            return;
        }
        String joinedTableNames = tables.stream()
                .map(table -> "\"" + table + "\"")
                .collect(Collectors.joining(", "));
        jdbcTemplate.execute("TRUNCATE TABLE " + joinedTableNames + " RESTART IDENTITY CASCADE");
    }

    protected void ensureRoleExists(String roleName) {
        roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
    }

    protected String randomEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID() + "@example.com";
    }

    protected String bearerToken(String token) {
        return "Bearer " + token;
    }

    protected JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected String extractToken(MvcResult result) throws Exception {
        return readJson(result).get("token").asText();
    }

    protected String registerUserAndGetToken(String email, String password, String firstName, String lastName) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload(email, password, firstName, lastName))))
                .andExpect(status().isCreated())
                .andReturn();
        return extractToken(result);
    }

    protected long createShortTermProperty(String token, String title, String city) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", bearerToken(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortTermPropertyPayload(title, city))))
                .andExpect(status().isCreated())
                .andReturn();
        return readJson(result).get("id").asLong();
    }

    protected long createActiveShortTermProperty(String token, String title, String city) throws Exception {
        long propertyId = createShortTermProperty(token, title, city);
        Property property = propertyRepository.findById(propertyId).orElseThrow();
        property.setStatus(PropertyStatus.ACTIVE);
        propertyRepository.save(property);
        return propertyId;
    }

    protected long createLongTermProperty(String token, String title, String city, double pricePerMonth) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", bearerToken(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(longTermPropertyPayload(title, city, pricePerMonth))))
                .andExpect(status().isCreated())
                .andReturn();
        return readJson(result).get("id").asLong();
    }

    protected long createActiveLongTermProperty(String token, String title, String city, double pricePerMonth) throws Exception {
        long propertyId = createLongTermProperty(token, title, city, pricePerMonth);
        Property property = propertyRepository.findById(propertyId).orElseThrow();
        property.setStatus(PropertyStatus.ACTIVE);
        propertyRepository.save(property);
        return propertyId;
    }

    protected long createBookingAndReturnId(String token, long propertyId, LocalDate from, LocalDate to, short guests) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", bearerToken(token))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingPayload(propertyId, from, to, guests))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = readJson(result);
        return json.get("id").asLong();
    }

    protected Map<String, Object> registerPayload(String email, String password, String firstName, String lastName) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        payload.put("firstName", firstName);
        payload.put("lastName", lastName);
        payload.put("phone", "+380991112233");
        return payload;
    }

    protected Map<String, Object> loginPayload(String email, String password) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        return payload;
    }

    protected Map<String, Object> shortTermPropertyPayload(String title, String city) {
        Map<String, Object> location = new LinkedHashMap<>();
        location.put("country", "Ukraine");
        location.put("region", "Kyivska");
        location.put("city", city);

        Map<String, Object> address = new LinkedHashMap<>();
        address.put("location", location);
        address.put("street", "Khreshchatyk");
        address.put("houseNumber", "10");
        address.put("apartment", "12");
        address.put("postalCode", "01001");
        address.put("lat", 50.4501);
        address.put("lng", 30.5234);

        Map<String, Object> pricing = new LinkedHashMap<>();
        pricing.put("pricePerNight", 1500.0);
        pricing.put("currency", "UAH");

        Map<String, Object> rules = new LinkedHashMap<>();
        rules.put("petsAllowed", true);
        rules.put("smokingAllowed", false);
        rules.put("partiesAllowed", false);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("address", address);
        payload.put("title", title);
        payload.put("description", "Integration test listing");
        payload.put("rentalType", "SHORT_TERM");
        payload.put("propertyType", "APARTMENT");
        payload.put("marketType", "SECONDARY");
        payload.put("rooms", 2);
        payload.put("floor", 4);
        payload.put("totalFloors", 9);
        payload.put("areaSqm", 48.5);
        payload.put("maxGuests", 3);
        payload.put("checkInTime", "11:00:00");
        payload.put("checkOutTime", "14:00:00");
        payload.put("pricing", pricing);
        payload.put("rules", rules);
        return payload;
    }

    protected Map<String, Object> longTermPropertyPayload(String title, String city, double pricePerMonth) {
        Map<String, Object> location = new LinkedHashMap<>();
        location.put("country", "Ukraine");
        location.put("region", "Kyivska");
        location.put("city", city);

        Map<String, Object> address = new LinkedHashMap<>();
        address.put("location", location);
        address.put("street", "Khreshchatyk");
        address.put("houseNumber", "10");
        address.put("apartment", "12");
        address.put("postalCode", "01001");
        address.put("lat", 50.4501);
        address.put("lng", 30.5234);

        Map<String, Object> pricing = new LinkedHashMap<>();
        pricing.put("pricePerMonth", pricePerMonth);
        pricing.put("currency", "UAH");

        Map<String, Object> rules = new LinkedHashMap<>();
        rules.put("petsAllowed", true);
        rules.put("smokingAllowed", false);
        rules.put("partiesAllowed", false);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("address", address);
        payload.put("title", title);
        payload.put("description", "Integration test listing");
        payload.put("rentalType", "LONG_TERM");
        payload.put("propertyType", "APARTMENT");
        payload.put("marketType", "SECONDARY");
        payload.put("rooms", 2);
        payload.put("floor", 4);
        payload.put("totalFloors", 9);
        payload.put("areaSqm", 48.5);
        payload.put("pricing", pricing);
        payload.put("rules", rules);
        return payload;
    }

    protected Map<String, Object> bookingPayload(long propertyId, LocalDate from, LocalDate to, short guests) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("propertyId", propertyId);
        payload.put("dateFrom", from.toString());
        payload.put("dateTo", to.toString());
        payload.put("guests", guests);
        return payload;
    }

    protected Map<String, Object> messagePayload(String text) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("text", text);
        return payload;
    }

    protected Map<String, Object> conversationPayload(long propertyId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("propertyId", propertyId);
        return payload;
    }

    protected Map<String, Object> reviewPayload(long propertyId, long bookingId, short rating, String comment) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("propertyId", propertyId);
        payload.put("bookingId", bookingId);
        payload.put("rating", rating);
        payload.put("comment", comment);
        return payload;
    }
}
