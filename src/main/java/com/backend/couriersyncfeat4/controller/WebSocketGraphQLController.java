package com.backend.couriersyncfeat4.controller;

import com.backend.couriersyncfeat4.config.Permission;
import com.backend.couriersyncfeat4.entity.AlertEntity;
import com.backend.couriersyncfeat4.entity.UserEntity;
import com.backend.couriersyncfeat4.exceptions.ApplicationException;
import com.backend.couriersyncfeat4.exceptions.ErrorCodes;
import com.backend.couriersyncfeat4.security.SecurityUtils;
import com.backend.couriersyncfeat4.service.AlertService;
import com.backend.couriersyncfeat4.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class WebSocketGraphQLController {

    private final Map<String, Sinks.Many<String>> userChannels = new ConcurrentHashMap<>();
    private final Map<String, Sinks.Many<AlertEntity>> userAlertChannels = new ConcurrentHashMap<>();
    private final AlertService alertService;
    private final UserService userService;

    @Autowired
    public WebSocketGraphQLController(AlertService alertService, UserService userService) {
        this.alertService = alertService;
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('package:update:all')")
    @MutationMapping
    public boolean sendMessageToUser(
            @Argument UUID userId,
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

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @SubscriptionMapping
    public Flux<String> subscribeToUserMessages(@Argument UUID userId) {
        assertCanSubscribe(userId);
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

    @PreAuthorize("hasAuthority('alert:create:all')")
    @MutationMapping
    public Boolean sendAlertToUser(
            @Argument UUID userId,
            @Argument UUID packageId,
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

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @SubscriptionMapping
    public Flux<AlertEntity> subscribeToUserAlerts(@Argument UUID userId) {
        assertCanSubscribe(userId);
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

    private void assertCanSubscribe(UUID userId) {
        if (SecurityUtils.hasPermission(Permission.PACKAGE_READ_ALL)) {
            return;
        }
        UserEntity currentUser = userService.getCurrentUser();
        if (currentUser == null || !currentUser.getId().equals(userId)) {
            throw new ApplicationException(ErrorCodes.FORBIDDEN);
        }
    }
}