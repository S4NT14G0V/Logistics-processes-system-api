package com.backend.couriersyncfeat4.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class SseEndpointIntegrationTest extends IntegrationTestBase {

    @Test
    @Operation("/events (sin token)")
    void subscribeRequiresAuthentication() {
        ResponseEntity<String> resp = rest.getForEntity("/events", String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    @Operation("/events (round-trip alert.created)")
    void receivesAlertEvent() throws Exception {
        HttpClient http = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/events"))
                .header("Authorization", "Bearer " + custToken)
                .GET()
                .build();

        AtomicReference<String> eventName = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        http.sendAsync(req, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(resp -> resp.body().forEach(line -> {
                    if (line.startsWith("event:")) {
                        eventName.set(line);
                        latch.countDown();
                    }
                }));

        Thread.sleep(1000);

        String pkg = createPackageUuid(adminToken);
        as(adminToken)
                .document("mutation($u: ID!, $p: ID!, $t: Int!, $d: String){ sendAlertToUser(userId: $u, packageId: $p, alertTypeId: $t, description: $d){ id } }")
                .variable("u", custId).variable("p", pkg).variable("t", 1).variable("d", "SSE test")
                .execute();

        assertThat(latch.await(15, TimeUnit.SECONDS)).isTrue();
        assertThat(eventName.get()).contains("alert.created");
    }
}
