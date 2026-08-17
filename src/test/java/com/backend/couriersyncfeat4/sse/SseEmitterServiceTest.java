package com.backend.couriersyncfeat4.sse;

import com.backend.couriersyncfeat4.integration.Operation;
import com.backend.couriersyncfeat4.integration.OperationLoggerExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(OperationLoggerExtension.class)
class SseEmitterServiceTest {

    private final SseEmitterService service = new SseEmitterService(new ObjectMapper());

    @Test
    @Operation("Send event when a package is registered to registered emitter")
    void shouldSendEventToRegisteredEmitter() throws Exception {
        SseEmitter emitter = mock(SseEmitter.class);
        UUID userId = UUID.randomUUID();

        service.register(userId, emitter);
        service.send(userId, "package.created", Map.of("id", "abc"));

        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @Operation("Reconnect replaces the previous emitter for the same user")
    void shouldReplaceExistingEmitterForSameUser() throws Exception {
        SseEmitter first = mock(SseEmitter.class);
        SseEmitter second = mock(SseEmitter.class);
        UUID userId = UUID.randomUUID();

        service.register(userId, first);
        service.register(userId, second);

        verify(first).complete();

        service.send(userId, "package.created", Map.of("id", "abc"));
        verify(second).send(any(SseEmitter.SseEventBuilder.class));
        verify(first, never()).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @Operation("Ignore send when there are no subscribers")
    void shouldIgnoreSendWhenNoSubscribers() {
        service.send(UUID.randomUUID(), "package.created", Map.of("id", "abc"));
    }
}
