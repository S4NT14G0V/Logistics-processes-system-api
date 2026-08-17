package com.backend.couriersyncfeat4.dto.output;

import java.util.UUID;

public record UserPackageCount(
    UUID userId,
    Integer packageCount
) {

}
