package com.backend.couriersyncfeat4.dto.input;

import jakarta.validation.constraints.NotBlank;

public record LocationAddInput(
        Float latitude,
        Float longitude,
        @NotBlank String address
) {
}
