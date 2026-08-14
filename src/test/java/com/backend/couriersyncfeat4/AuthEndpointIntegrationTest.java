package com.backend.couriersyncfeat4;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthEndpointIntegrationTest extends IntegrationTestBase {

    @Test
    void register() {
        JsonNode r = register("reg-" + UUID.randomUUID() + "@test.com");
        assertThat(r.hasNonNull("accessToken")).isTrue();
        assertThat(r.hasNonNull("refreshToken")).isTrue();
    }

    @Test
    void login() {
        JsonNode r = login(ADMIN_EMAIL);
        assertThat(r.hasNonNull("accessToken")).isTrue();
        assertThat(r.hasNonNull("refreshToken")).isTrue();
    }

    @Test
    void refresh() {
        String refresh = login(ADMIN_EMAIL).get("refreshToken").asText();
        JsonNode r = post("/auth/refresh", Map.of("refreshToken", refresh), null);
        assertThat(r.hasNonNull("accessToken")).isTrue();
    }

    @Test
    void logout() {
        String refresh = login(ADMIN_EMAIL).get("refreshToken").asText();
        assertThat(postStatus("/auth/logout", Map.of("refreshToken", refresh))).isEqualTo(204);
    }
}
