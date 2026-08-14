package com.backend.couriersyncfeat4.dto.output;

import java.time.LocalDateTime;
import java.util.List;

public record PackageTrackingResponse(
        String trackingCode,
        String description,
        LocalDateTime registeredAt,
        LocalDateTime updatedAt,
        LocalDateTime cancelledAt,
        PackageStatusResponse status,
        PlaceResponse origin,
        PlaceResponse destination,
        List<PackageStatusHistoryResponse> history) {
}
