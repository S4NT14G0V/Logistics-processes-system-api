package com.backend.couriersyncfeat4.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SseEmitterServiceTest {

    private final SseEmitterService service = new SseEmitterService(new ObjectMapper());

    @Test
    void shouldSendEventToRegisteredEmitter() throws Exception {
        SseEmitter emitter = mock(SseEmitter.class);
        UUID userId = UUID.randomUUID();

        service.register(userId, emitter);
        service.send(userId, "package.created", Map.of("id", "abc"));

        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void shouldIgnoreSendWhenNoSubscribers() {
        service.send(UUID.randomUUID(), "package.created", Map.of("id", "abc"));
    }
}
