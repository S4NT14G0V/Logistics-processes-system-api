package com.backend.couriersyncfeat4.dto.output;

import java.time.LocalDateTime;

public record PackageStatusHistoryResponse(
    LocalDateTime changedAt,
    PackageStatusResponse fromStatus,
    PackageStatusResponse toStatus,
    String changedBy
) {

}
