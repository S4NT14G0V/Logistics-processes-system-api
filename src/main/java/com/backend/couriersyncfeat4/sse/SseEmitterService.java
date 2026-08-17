package com.backend.couriersyncfeat4.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterService {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public SseEmitterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(0L);
        register(userId, emitter);
        return emitter;
    }

    void register(UUID userId, SseEmitter emitter) {
        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> {
            remove(userId, emitter);
            emitter.complete();
        });
        emitter.onError(ex -> remove(userId, emitter));

        SseEmitter previous = emitters.put(userId, emitter);
        if (previous != null) {
            previous.complete();
        }
    }

    public void send(UUID userId, String eventName, Object payload) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            return;
        }
        String json = toJson(payload);
        try {
            emitter.send(SseEmitter.event().name(eventName).data(json));
        } catch (IOException | IllegalStateException e) {
            emitter.completeWithError(e);
            remove(userId, emitter);
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Error serializing SSE event", e);
        }
    }

    private void remove(UUID userId, SseEmitter emitter) {
        emitters.remove(userId, emitter);
    }
}
