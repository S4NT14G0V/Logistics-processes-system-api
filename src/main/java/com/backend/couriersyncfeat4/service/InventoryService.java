package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.dto.output.InventorySummaryResponse;
import com.backend.couriersyncfeat4.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService {

    private static final LocalDateTime MIN_DATE = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime MAX_DATE = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    private final InventoryRepository inventoryRepo;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    public List<InventorySummaryResponse> findSummary(LocalDateTime start, LocalDateTime end, String region) {
        LocalDateTime from = start != null ? start : MIN_DATE;
        LocalDateTime to = end != null ? end : MAX_DATE;
        return inventoryRepo.summaryByPeriodAndRegion(from, to, region);
    }
}
