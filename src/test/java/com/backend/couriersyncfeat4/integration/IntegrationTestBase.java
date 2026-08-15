package com.backend.couriersyncfeat4.integration;

import com.backend.couriersyncfeat4.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(OperationLoggerExtension.class)
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

    protected static final String PACKAGE_FIELDS =
            "{ uuid trackingCode description registeredAt status{ code name } origin{ uuid name } destination{ uuid name } history{ changedAt fromStatus{ code } toStatus{ code } } price weightKg }";
    protected static final String TRACKING_FIELDS =
            "{ trackingCode description status{ code name } history{ changedAt toStatus{ code } } }";
    protected static final String PLACE_FIELDS =
            "{ uuid name address city department latitude longitude }";

    @Autowired
    protected TestRestTemplate rest;

    @LocalServerPort
    protected int port;

    @Autowired
    protected UserRepository userRepository;

    protected HttpGraphQlTester graphQlTester;

    protected String adminToken;
    protected String custToken;
    protected String originId;
    protected String destId;
    protected String adminId;
    protected String custId;

    protected record PkgRef(String uuid, String trackingCode) {
    }

    @BeforeAll
    void setup() {
        graphQlTester = HttpGraphQlTester.create(
                WebTestClient.bindToServer().baseUrl("http://localhost:" + port + "/graphql").build());

        if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
            register(ADMIN_EMAIL);
        }
        if (!userRepository.existsByEmail(CUST_EMAIL)) {
            register(CUST_EMAIL);
        }

        adminToken = login(ADMIN_EMAIL).get("accessToken").asText();
        custToken = login(CUST_EMAIL).get("accessToken").asText();

        originId = createPlaceUuid(adminToken, "Origen " + UUID.randomUUID(), 4.7110, -74.0721);
        destId = createPlaceUuid(adminToken, "Destino " + UUID.randomUUID(), 6.2442, -75.5812);

        adminId = userRepository.findByEmail(ADMIN_EMAIL).orElseThrow().getId().toString();
        custId = userRepository.findByEmail(CUST_EMAIL).orElseThrow().getId().toString();
    }

    // ---------- GraphQL ----------

    protected HttpGraphQlTester as(String token) {
        if (token == null) {
            return graphQlTester;
        }
        return graphQlTester.mutate().headers(h -> h.setBearerAuth(token)).build();
    }

    protected PkgRef propose(String token) {
        GraphQlTester.Response r = as(token)
                .document("mutation($i: PackageInput!){ proposePackage(input: $i){ uuid trackingCode } }")
                .variable("i", baseInput())
                .execute();
        return new PkgRef(
                r.path("proposePackage.uuid").entity(String.class).get(),
                r.path("proposePackage.trackingCode").entity(String.class).get());
    }

    protected String createPackageUuid(String token) {
        return as(token)
                .document("mutation($i: PackageInput!){ createPackage(input: $i){ uuid } }")
                .variable("i", baseInput())
                .execute()
                .path("createPackage.uuid").entity(String.class).get();
    }

    protected String createPlaceUuid(String token, String name, double lat, double lng) {
        Map<String, Object> input = placeInput(name);
        input.put("latitude", lat);
        input.put("longitude", lng);
        return as(token)
                .document("mutation($i: PlaceInput!){ createPlace(input: $i){ uuid } }")
                .variable("i", input)
                .execute()
                .path("createPlace.uuid").entity(String.class).get();
    }

    // ---------- REST (auth) ----------

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

    // ---------- aserciones de contrato ----------

    protected void assertPackageShape(GraphQlTester.Response resp, String op, String statusCode) {
        resp.path(op + ".uuid").entity(String.class).satisfies(u -> assertThat(u).isNotBlank());
        resp.path(op + ".trackingCode").entity(String.class).satisfies(u -> assertThat(u).isNotBlank());
        resp.path(op + ".status.code").entity(String.class).isEqualTo(statusCode);
        resp.path(op + ".status.name").entity(String.class).satisfies(u -> assertThat(u).isNotBlank());
        resp.path(op + ".origin.uuid").entity(String.class).satisfies(u -> assertThat(u).isNotBlank());
        resp.path(op + ".destination.uuid").entity(String.class).satisfies(u -> assertThat(u).isNotBlank());
        resp.path(op + ".history").entityList(Object.class).satisfies(list -> assertThat(list).hasSizeGreaterThan(0));
    }

    protected void assertTrackingShape(GraphQlTester.Response resp) {
        resp.path("findPackageByTrackingCode.trackingCode").entity(String.class).satisfies(u -> assertThat(u).isNotBlank());
        resp.path("findPackageByTrackingCode.status.code").entity(String.class).satisfies(u -> assertThat(u).isNotBlank());
        resp.path("findPackageByTrackingCode.history").entityList(Object.class).satisfies(list -> assertThat(list).hasSizeGreaterThan(0));
    }

    protected void assertPlaceShape(GraphQlTester.Response resp, String op) {
        resp.path(op + ".uuid").entity(String.class).satisfies(u -> assertThat(u).isNotBlank());
        resp.path(op + ".name").entity(String.class).satisfies(u -> assertThat(u).isNotBlank());
        resp.path(op + ".address").entity(String.class).satisfies(u -> assertThat(u).isNotBlank());
        resp.path(op + ".city").entity(String.class).satisfies(u -> assertThat(u).isNotBlank());
        resp.path(op + ".department").entity(String.class).satisfies(u -> assertThat(u).isNotBlank());
    }

    protected void expectForbidden(GraphQlTester.Errors errors) {
        errors.satisfy(list -> {
            assertThat(list).isNotEmpty();
            assertThat(list.get(0).getMessage()).isEqualTo("Forbidden");
        });
    }

    protected void expectErrorCode(GraphQlTester.Errors errors, String code) {
        errors.satisfy(list -> {
            assertThat(list).isNotEmpty();
            assertThat(list.get(0).getExtensions()).containsEntry("code", code);
        });
    }

    // ---------- datos de prueba ----------

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
