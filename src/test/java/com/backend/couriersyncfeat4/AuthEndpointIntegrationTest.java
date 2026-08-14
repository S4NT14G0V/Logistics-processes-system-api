package com.backend.couriersyncfeat4;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthEndpointIntegrationTest extends IntegrationTestBase {

    @Test
    @Operation("/auth/register")
    void register() {
        JsonNode r = register("reg-" + UUID.randomUUID() + "@test.com");
        assertThat(r.get("accessToken").asText()).isNotBlank();
        assertThat(r.get("refreshToken").asText()).isNotBlank();
    }

    @Test
    @Operation("/auth/login")
    void login() {
        JsonNode r = login(ADMIN_EMAIL);
        assertThat(r.get("accessToken").asText()).isNotBlank();
        assertThat(r.get("refreshToken").asText()).isNotBlank();
    }

    @Test
    @Operation("/auth/refresh")
    void refresh() {
        String refresh = login(ADMIN_EMAIL).get("refreshToken").asText();
        JsonNode r = post("/auth/refresh", Map.of("refreshToken", refresh), null);
        assertThat(r.get("accessToken").asText()).isNotBlank();
    }

    @Test
    @Operation("/auth/logout")
    void logout() {
        String refresh = login(ADMIN_EMAIL).get("refreshToken").asText();
        assertThat(postStatus("/auth/logout", Map.of("refreshToken", refresh))).isEqualTo(204);
    }
}
