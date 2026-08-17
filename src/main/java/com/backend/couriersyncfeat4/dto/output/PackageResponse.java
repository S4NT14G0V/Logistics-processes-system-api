package com.backend.couriersyncfeat4.dto.output;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PackageResponse(
        UUID uuid,
        String trackingCode,
        String description,
        LocalDateTime registeredAt,
        LocalDateTime updatedAt,
        LocalDateTime cancelledAt,
        String cancellationReason,
        Double weightKg,
        Double lengthCm,
        Double widthCm,
        Double heightCm,
        Double distanceKm,
        Double declaredValue,
        Double price,
        PackageStatusResponse status,
        PlaceResponse origin,
        PlaceResponse destination,
        List<PackageStatusHistoryResponse> history) {
}
