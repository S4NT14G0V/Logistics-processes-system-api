package com.backend.couriersyncfeat4.controller;

import com.backend.couriersyncfeat4.dto.output.AlertResponse;
import com.backend.couriersyncfeat4.service.AlertService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PreAuthorize("hasAuthority('alert:read:all')")
    @QueryMapping
    public List<AlertResponse> findAllAlerts() {
        return alertService.findAll();
    }

    @PreAuthorize("hasAnyAuthority('alert:read:all','alert:read:own')")
    @QueryMapping
    public List<AlertResponse> findAllAlertsByUserId(@Argument UUID userId) {
        return alertService.findAllAlertsByUserId(userId);
    }
}
