package com.backend.couriersyncfeat4.dto.input;

import java.util.UUID;

public record PackageUpdateInput(
    String description,
    UUID origin,
    UUID destination
) {

}
