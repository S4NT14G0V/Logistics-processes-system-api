package com.backend.couriersyncfeat4.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OperationLoggerExtension.class)
class SseEndpointIntegrationTest extends IntegrationTestBase {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @Test
    @Operation("/events (sin token)")
    void subscribeRequiresAuthentication() {
        ResponseEntity<String> resp = rest.getForEntity("/events", String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    @Operation("/events (round-trip alert.created)")
    void receivesAlertEvent() throws Exception {
        String event = captureEvent("alert.created", () -> {
            String pkg = createPackageUuid(adminToken);
            as(adminToken)
                    .document("mutation($u: ID!, $p: ID!, $t: Int!, $d: String){ sendAlertToUser(userId: $u, packageId: $p, alertTypeId: $t, description: $d){ id } }")
                    .variable("u", custId).variable("p", pkg).variable("t", 1).variable("d", "SSE test")
                    .execute();
        });

        assertThat(event).isNotNull();
        assertThat(event).contains("alert.created");
    }

    @Test
    @Operation("/events (round-trip package.status-changed)")
    void receivesPackageStatusEvent() throws Exception {
        String event = captureEvent("package.status-changed", () -> {
            Map<String, Object> input = baseInput();
            input.put("ownerUserId", custId);

            String pkg = as(adminToken)
                    .document("mutation($i: PackageInput!){ createPackage(input: $i){ uuid } }")
                    .variable("i", input).execute()
                    .path("createPackage.uuid").entity(String.class).get();

            as(adminToken)
                    .document("mutation($id: ID!, $c: String!){ changePackageStatus(id: $id, statusCode: $c){ uuid } }")
                    .variable("id", pkg).variable("c", "IN_TRANSIT").execute();
        });

        assertThat(event).isNotNull();
        assertThat(event).contains("package.status-changed");
    }

    private String captureEvent(String expectedEvent, Runnable trigger) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/events"))
                .header("Authorization", "Bearer " + custToken)
                .GET()
                .build();

        AtomicReference<String> event = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Stream<String>> bodyRef = new AtomicReference<>();

        CompletableFuture<HttpResponse<Stream<String>>> future =
                HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofLines());
        future.thenAccept(resp -> {
            Stream<String> lines = resp.body();
            bodyRef.set(lines);
            lines.forEach(line -> {
                if (line.contains(expectedEvent)) {
                    event.set(line);
                    latch.countDown();
                }
            });
        });

        try {
            Thread.sleep(1000);
            trigger.run();

            assertThat(latch.await(15, TimeUnit.SECONDS)).isTrue();
            return event.get();
        } finally {
            Stream<String> lines = bodyRef.get();
            if (lines != null) {
                lines.close();
            }
            future.cancel(true);
        }
    }
}
