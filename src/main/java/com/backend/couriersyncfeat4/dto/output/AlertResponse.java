package com.backend.couriersyncfeat4.dto.output;

import java.time.LocalDateTime;
import java.util.UUID;

public record AlertResponse(
        Long id,
        String description,
        LocalDateTime registeredAt,
        AlertTypeResponse alertType,
        UUID packageId
) {
}
