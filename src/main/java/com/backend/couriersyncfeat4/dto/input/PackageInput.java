package com.backend.couriersyncfeat4.dto.input;

import java.util.UUID;

public record PackageInput(
    String description,
    UUID origin,
    UUID destination,
    UUID ownerUserId,
    Double weightKg,
    Double lengthCm,
    Double widthCm,
    Double heightCm,
    Double declaredValue
) {

}
