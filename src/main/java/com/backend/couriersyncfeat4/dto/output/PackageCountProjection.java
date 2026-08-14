package com.backend.couriersyncfeat4.dto.output;

import java.util.UUID;

public interface PackageCountProjection {
    UUID getUserId();
    Long getPackageCount();
}
