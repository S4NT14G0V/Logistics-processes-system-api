package com.backend.couriersyncfeat4.repository;

import com.backend.couriersyncfeat4.dto.output.InventorySummaryResponse;
import com.backend.couriersyncfeat4.entity.PackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<PackageEntity, UUID> {

    @Query("SELECT p.destination.name AS region, " +
            "SUM(CASE WHEN p.status.code = 'IN_TRANSIT' THEN 1 ELSE 0 END) AS inTransit, " +
            "SUM(CASE WHEN p.status.code = 'DELIVERED' THEN 1 ELSE 0 END) AS delivered, " +
            "SUM(CASE WHEN p.status.code = 'CREATED' THEN 1 ELSE 0 END) AS pending " +
            "FROM PackageEntity p " +
            "WHERE p.registeredAt >= :start " +
            "  AND p.registeredAt <= :end " +
            "  AND (:region IS NULL OR p.destination.name = :region) " +
            "GROUP BY p.destination.name")
    List<InventorySummaryResponse> summaryByPeriodAndRegion(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("region") String region);
}
