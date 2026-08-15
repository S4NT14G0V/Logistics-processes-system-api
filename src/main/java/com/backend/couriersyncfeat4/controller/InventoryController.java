package com.backend.couriersyncfeat4.controller;

import com.backend.couriersyncfeat4.dto.output.InventorySummaryResponse;
import com.backend.couriersyncfeat4.service.InventoryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class InventoryController {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PreAuthorize("hasAuthority('inventory:read')")
    @QueryMapping
    public List<InventorySummaryResponse> inventorySummary(
            @Argument String periodStart,
            @Argument String periodEnd,
            @Argument String region) {

        LocalDateTime start = periodStart == null ? null : LocalDateTime.parse(periodStart, ISO);
        LocalDateTime end = periodEnd == null ? null : LocalDateTime.parse(periodEnd, ISO);

        return inventoryService.findSummary(start, end, region);
    }
}
