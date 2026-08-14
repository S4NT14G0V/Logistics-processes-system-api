package com.backend.couriersyncfeat4.controller;

import com.backend.couriersyncfeat4.dto.input.PackageInput;
import com.backend.couriersyncfeat4.dto.input.PackageUpdateInput;
import com.backend.couriersyncfeat4.dto.output.PackageCountResponse;
import com.backend.couriersyncfeat4.dto.output.PackageResponse;
import com.backend.couriersyncfeat4.dto.output.PackageStatsResponse;
import com.backend.couriersyncfeat4.dto.output.PackageStatusHistoryResponse;
import com.backend.couriersyncfeat4.dto.output.PackageTrackingResponse;
import com.backend.couriersyncfeat4.dto.output.StatusCount;
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

    @PreAuthorize("hasAuthority('package:create:all')")
    @MutationMapping
    public PackageResponse createPackage(@Argument("input") PackageInput input) {
        return packageService.createPackage(input);
    }

    @PreAuthorize("hasAuthority('package:create:own')")
    @MutationMapping
    public PackageResponse proposePackage(@Argument("input") PackageInput input) {
        return packageService.proposePackage(input);
    }

    @PreAuthorize("hasAuthority('package:update:all')")
    @MutationMapping
    public PackageResponse approvePackage(@Argument UUID id) {
        return packageService.approvePackage(id);
    }

    @PreAuthorize("hasAuthority('package:update:all')")
    @MutationMapping
    public PackageResponse rejectPackage(@Argument UUID id, @Argument String reason) {
        return packageService.rejectPackage(id, reason);
    }

    @PreAuthorize("hasAuthority('package:update:all')")
    @MutationMapping
    public PackageResponse reactivatePackage(@Argument UUID id) {
        return packageService.reactivatePackage(id);
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

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public List<PackageStatusHistoryResponse> findPackageHistory(@Argument UUID packageId) {
        return packageService.findPackageHistory(packageId);
    }

    @QueryMapping
    public PackageTrackingResponse findPackageByTrackingCode(@Argument String trackingCode) {
        return packageService.findPackageByTrackingCode(trackingCode);
    }

    @PreAuthorize("hasAnyAuthority('package:update:all','package:update:own')")
    @MutationMapping
    public PackageResponse updatePackage(@Argument UUID id, @Argument("input") PackageUpdateInput input) {
        return packageService.updatePackage(id, input);
    }

    @PreAuthorize("hasAnyAuthority('package:cancel:all','package:cancel:own')")
    @MutationMapping
    public PackageResponse cancelPackage(@Argument UUID id, @Argument String reason) {
        return packageService.cancelPackage(id, reason);
    }

    @PreAuthorize("hasAuthority('package:update:all')")
    @MutationMapping
    public PackageResponse changePackageStatus(@Argument UUID id, @Argument String statusCode) {
        return packageService.changePackageStatus(id, statusCode);
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public List<PackageResponse> findPackagesByDateRange(@Argument Integer page, @Argument Integer size,
            @Argument LocalDateTime startDate, @Argument LocalDateTime endDate) {
        return packageService.findPackagesByDateRange(page, size, startDate, endDate);
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public PackageCountResponse findPackageCountByUserId(@Argument UUID userId) {
        return packageService.findPackageCountByUserId(userId);
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public List<PackageResponse> findPackagesByStatusIn(@Argument Integer page, @Argument Integer size,
            @Argument List<String> packageStatuses) {
        return packageService.findPackagesByStatusIn(page, size, packageStatuses);
    }

    @PreAuthorize("hasAnyAuthority('package:read:all')")
    @QueryMapping
    public PackageStatsResponse findPackageCountByAllUsers() {
        return packageService.findPackageCountByAllUsers();
    }

    @PreAuthorize("hasAnyAuthority('package:read:all')")
    @QueryMapping
    public List<StatusCount> findPackageCountByAllStatus() {
        return packageService.findPackageCountByAllStatus();
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public List<PackageResponse> findAllPackagesByUserId(@Argument Integer page, @Argument Integer size,
            @Argument UUID userId) {
        return packageService.findAllPackagesByUserId(page, size, userId);
    }

    @PreAuthorize("hasAnyAuthority('package:read:all','package:read:own')")
    @QueryMapping
    public List<PackageResponse> findAllPackagesByPlace(@Argument Integer page, @Argument Integer size,
            @Argument UUID origin, @Argument UUID destination) {
        return packageService.findAllPackagesByPlace(page, size, origin, destination);
    }
}
