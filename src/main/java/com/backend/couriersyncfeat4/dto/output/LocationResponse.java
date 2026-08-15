package com.backend.couriersyncfeat4.dto.output;

public record LocationResponse(
        Long id,
        Float latitude,
        Float longitude,
        String address
) {
}
