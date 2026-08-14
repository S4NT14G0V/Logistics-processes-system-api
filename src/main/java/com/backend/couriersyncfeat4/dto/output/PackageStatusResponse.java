package com.backend.couriersyncfeat4.dto.output;

public record PackageStatusResponse(
    int id, 
    String code, 
    String name, 
    String description
) {
}
