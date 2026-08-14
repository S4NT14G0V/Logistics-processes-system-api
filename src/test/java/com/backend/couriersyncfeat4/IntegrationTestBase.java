package com.backend.couriersyncfeat4;

import com.backend.couriersyncfeat4.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTestBase {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    protected static final String ADMIN_EMAIL = "admin@test.com";
    protected static final String CUST_EMAIL = "customer@test.com";
    protected static final String PASSWORD = "Passw0rd!123";

    @Autowired
    protected TestRestTemplate rest;

    @Autowired
    protected UserRepository userRepository;

    protected String adminToken;
    protected String custToken;
    protected String originId;
    protected String destId;
    protected String adminId;
    protected String custId;

    @BeforeAll
    void setup() {
        if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
            register(ADMIN_EMAIL);
        }
        if (!userRepository.existsByEmail(CUST_EMAIL)) {
            register(CUST_EMAIL);
        }

        adminToken = login(ADMIN_EMAIL).get("accessToken").asText();
        custToken = login(CUST_EMAIL).get("accessToken").asText();

        originId = createPlace(adminToken, "Origen " + UUID.randomUUID(), 4.7110, -74.0721);
        destId = createPlace(adminToken, "Destino " + UUID.randomUUID(), 6.2442, -75.5812);

        adminId = userRepository.findByEmail(ADMIN_EMAIL).orElseThrow().getId().toString();
        custId = userRepository.findByEmail(CUST_EMAIL).orElseThrow().getId().toString();
    }

    // ---------- helpers ----------

    protected boolean errors(JsonNode resp) {
        return resp == null || resp.hasNonNull("errors");
    }

    protected JsonNode graphql(String token, String query, Map<String, Object> vars) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            h.setBearerAuth(token);
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("query", query);
        if (vars != null) {
            payload.put("variables", vars);
        }
        ResponseEntity<JsonNode> resp = rest.exchange("/graphql", HttpMethod.POST, new HttpEntity<>(payload, h), JsonNode.class);
        return resp.getBody();
    }

    protected JsonNode register(String email) {
        return post("/auth/register", Map.of("name", "Test User", "email", email, "password", PASSWORD), null);
    }

    protected JsonNode login(String email) {
        return post("/auth/login", Map.of("email", email, "password", PASSWORD), null);
    }

    protected JsonNode post(String path, Map<String, Object> body, String token) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            h.setBearerAuth(token);
        }
        ResponseEntity<JsonNode> resp = rest.exchange(path, HttpMethod.POST, new HttpEntity<>(body, h), JsonNode.class);
        return resp.getBody();
    }

    protected int postStatus(String path, Map<String, Object> body) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> resp = rest.exchange(path, HttpMethod.POST, new HttpEntity<>(body, h), String.class);
        return resp.getStatusCode().value();
    }

    protected JsonNode propose(String token) {
        JsonNode r = graphql(token, "mutation($i: PackageInput!){ proposePackage(input: $i){ uuid trackingCode status{code} } }",
                Map.of("i", baseInput()));
        assertThat(errors(r)).isFalse();
        return r.get("data").get("proposePackage");
    }

    protected JsonNode createPackage(String token) {
        JsonNode r = graphql(token, "mutation($i: PackageInput!){ createPackage(input: $i){ uuid status{code} } }",
                Map.of("i", baseInput()));
        assertThat(errors(r)).isFalse();
        return r.get("data").get("createPackage");
    }

    protected String createPlace(String token, String name, double lat, double lng) {
        Map<String, Object> input = placeInput(name);
        input.put("latitude", lat);
        input.put("longitude", lng);
        JsonNode r = graphql(token, "mutation($i: PlaceInput!){ createPlace(input: $i){ uuid } }", Map.of("i", input));
        assertThat(errors(r)).isFalse();
        return r.get("data").get("createPlace").get("uuid").asText();
    }

    protected Map<String, Object> baseInput() {
        Map<String, Object> m = new HashMap<>();
        m.put("description", "Paquete de test");
        m.put("origin", originId);
        m.put("destination", destId);
        m.put("weightKg", 2.5);
        m.put("lengthCm", 20);
        m.put("widthCm", 15);
        m.put("heightCm", 10);
        m.put("declaredValue", 100000);
        return m;
    }

    protected Map<String, Object> placeInput(String name) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("address", "Addr");
        m.put("city", "City");
        m.put("department", "Dept");
        m.put("latitude", 4.7);
        m.put("longitude", -74.0);
        return m;
    }
}
