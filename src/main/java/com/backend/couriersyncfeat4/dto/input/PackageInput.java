package com.backend.couriersyncfeat4.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record PackageInput(
    String description,
    @NotBlank UUID origin,
    @NotBlank UUID destination,
    UUID ownerUserId,
    @PositiveOrZero Double weightKg,
    @Positive Double lengthCm,
    @Positive Double widthCm,
    @Positive Double heightCm,
    @PositiveOrZero Double declaredValue
) {

}
