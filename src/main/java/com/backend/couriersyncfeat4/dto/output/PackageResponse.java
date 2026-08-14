package com.backend.couriersyncfeat4.dto.output;

import java.time.LocalDateTime;
import java.util.UUID;

public record PackageResponse(
        UUID uuid,
        String trackingCode,
        String description,
        LocalDateTime registeredAt,
        LocalDateTime updatedAt,
        LocalDateTime cancelledAt,
        String cancellationReason,
        PackageStatusResponse status,
        PlaceResponse origin,
        PlaceResponse destination) {
}
