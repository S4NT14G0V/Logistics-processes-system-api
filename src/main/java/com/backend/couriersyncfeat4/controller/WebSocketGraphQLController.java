package com.backend.couriersyncfeat4.controller;

import com.backend.couriersyncfeat4.entity.AlertEntity;
import com.backend.couriersyncfeat4.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class WebSocketGraphQLController {

    private final Map<String, Sinks.Many<String>> userChannels = new ConcurrentHashMap<>();
    private final Map<String, Sinks.Many<AlertEntity>> userAlertChannels = new ConcurrentHashMap<>();
    private final AlertService alertService;

    @Autowired
    public WebSocketGraphQLController(AlertService alertService) {
        this.alertService = alertService;
    }

    @MutationMapping
    public boolean sendMessageToUser(
            @Argument Long userId,
            @Argument String message) {

        if (userId == null || message == null) {
            throw new IllegalArgumentException("UserId y message son requeridos");
        }

        Sinks.Many<String> sink = userChannels.get(userId.toString());
        if (sink != null) {
            Sinks.EmitResult result = sink.tryEmitNext(message);
            return !result.isFailure();
        }
        return false;
    }

    @SubscriptionMapping
    public Flux<String> subscribeToUserMessages(@Argument Long userId) {
        return userChannels
                .computeIfAbsent(userId.toString(), id -> {
                    Sinks.Many<String> newSink = Sinks.many().multicast().onBackpressureBuffer();

                    // Limpieza cuando el cliente se desconecta
                    newSink.asFlux()
                            .doOnCancel(() -> userChannels.remove(id, newSink))
                            .doOnError(ex -> userChannels.remove(id, newSink))
                            .subscribe();

                    return newSink;
                })
                .asFlux();
    }

    @MutationMapping
    public Boolean sendAlertToUser(
            @Argument Long userId,
            @Argument Long packageId,
            @Argument int alertTypeId,
            @Argument String description) {

        try {
            AlertEntity alert = alertService.createAlert(userId, packageId, alertTypeId, description);

            Sinks.Many<AlertEntity> sink = userAlertChannels.get(userId.toString());
            if (sink != null) {
                Sinks.EmitResult result = sink.tryEmitNext(alert);
                return !result.isFailure();
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar alerta: " + e.getMessage());
        }
    }

    @SubscriptionMapping
    public Flux<AlertEntity> subscribeToUserAlerts(@Argument Long userId) {
        return userAlertChannels
                .computeIfAbsent(userId.toString(), id -> {
                    Sinks.Many<AlertEntity> newSink = Sinks.many().multicast().onBackpressureBuffer();

                    // Limpieza cuando el cliente se desconecta
                    newSink.asFlux()
                            .doOnCancel(() -> userAlertChannels.remove(userId.toString(), newSink))
                            .doOnError(ex -> userAlertChannels.remove(userId.toString(), newSink))
                            .subscribe();

                    return newSink;
                })
                .asFlux();
    }
}