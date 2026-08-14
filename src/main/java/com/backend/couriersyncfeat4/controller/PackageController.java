package com.backend.couriersyncfeat4.controller;

import com.backend.couriersyncfeat4.dto.input.PackageInput;
import com.backend.couriersyncfeat4.dto.output.PackageCountResponse;
import com.backend.couriersyncfeat4.dto.output.PackageResponse;
import com.backend.couriersyncfeat4.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
public class PackageController {

    private final PackageService packageService;

    @Autowired
    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @PreAuthorize("hasAuthority('package:create')")
    @MutationMapping("createPackage")
    public PackageResponse createPackage(@Argument("input") PackageInput input) {
        return packageService.createPackage(input);
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public List<PackageResponse> findAllPackages(@Argument Integer page, @Argument Integer size) {
        return packageService.findAllPackages(page, size);
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public PackageResponse findPackageById(@Argument UUID id) {
        return packageService.findPackageById(id);
    }

    @PreAuthorize("hasAnyAuthority('package:update','package:read:own')")
    @MutationMapping
    public PackageResponse updatePackage(@Argument UUID id, @Argument("input") PackageInput input) {
        return packageService.updatePackage(id, input);
    }

    @PreAuthorize("hasAnyAuthority('package:cancel')")
    @MutationMapping
    public PackageResponse deletePackageById(@Argument UUID id, @Argument String reason) {
        return packageService.deletePackageById(id, reason);
    }

    @PreAuthorize("hasAuthority('package:update')")
    @MutationMapping
    public PackageResponse changePackageStatus(@Argument UUID id, @Argument String statusCode) {
        return packageService.changePackageStatus(id, statusCode);
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public PackageResponse findPackageByTrackingCode(@Argument String trackingCode) {
        return packageService.findPackageByTrackingCode(trackingCode);
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public List<PackageResponse> findPackagesByDateRange(@Argument LocalDateTime startDate,
            @Argument LocalDateTime endDate) {
        return packageService.findPackagesByDateRange(startDate, endDate);
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public PackageCountResponse findPackageCountByUserId(@Argument UUID userId) {
        return packageService.findPackageCountByUserId(userId);
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public List<PackageResponse> findPackagesByStatusIn(@Argument List<String> packageStatuses) {
        return packageService.findPackagesByStatusIn(packageStatuses);
    }

    @PreAuthorize("hasAnyAuthority('package:read:all')")
    @QueryMapping
    public List<PackageCountResponse> findPackageCountByAllUsers() {
        return packageService.findCountByAllUsers();
    }

    @PreAuthorize("hasAnyAuthority('package:read:all')")
    @QueryMapping
    public List<PackageResponse> findAllPackagesByUserId(@Argument UUID userId) {
        return packageService.findAllPackagesByUserId(userId);
    }

    @PreAuthorize("hasAnyAuthority('package:read:all')")
    @QueryMapping
    public List<PackageResponse> findAllPackagesByUbication(@Argument UUID origin, @Argument UUID destination) {
        return packageService.findAllPackagesByUbication(origin, destination);
    }
}
