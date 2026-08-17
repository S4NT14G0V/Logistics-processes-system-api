package com.backend.couriersyncfeat4.dto.output;

import java.util.List;

public record PackageStatsResponse(
    List<UserPackageCount> users,
    Integer totalPackages
) {

}
