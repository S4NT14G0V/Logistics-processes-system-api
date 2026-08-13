package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.entity.InventorySummary;
import com.backend.couriersyncfeat4.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepo;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    public List<InventorySummary> findSummary(LocalDateTime start, LocalDateTime end, String region) {
        return inventoryRepo.summaryByPeriodAndRegion(start, end, region);
    }
}
