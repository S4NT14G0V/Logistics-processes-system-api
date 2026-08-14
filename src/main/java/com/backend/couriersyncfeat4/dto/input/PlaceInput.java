package com.backend.couriersyncfeat4.dto.input;

import jakarta.validation.constraints.NotBlank;

public record PlaceInput(
    @NotBlank String name,
    @NotBlank String address,
    @NotBlank String city,
    @NotBlank String department,
    Float latitude,
    Float longitude
) {

}
