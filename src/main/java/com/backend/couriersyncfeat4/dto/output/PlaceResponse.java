package com.backend.couriersyncfeat4.dto.output;

import java.util.UUID;

public record PlaceResponse(
    UUID uuid,
    String name,
    String address,
    String city,
    String department,
    Float latitude,
    Float longitude
) {
}
