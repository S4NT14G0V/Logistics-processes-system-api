package com.backend.couriersyncfeat4.controller;

import com.backend.couriersyncfeat4.dto.PackageCountByUserDTO;
import com.backend.couriersyncfeat4.entity.CustomResponseEntity;
import com.backend.couriersyncfeat4.entity.PackageEntity;
import com.backend.couriersyncfeat4.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class PackageController {

    private final PackageService packageService;

    @Autowired
    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR', 'SUPERVISOR')")
    @MutationMapping
    public PackageEntity addPackage(@Argument PackageEntity packageEntity){
        return packageService.addPackage(packageEntity);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @QueryMapping
    public List<PackageEntity> findAllPackages() {
        return packageService.findAllPackages();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @QueryMapping
    public PackageEntity findPackageById(@Argument Long id) {
        return packageService.findPackageById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @MutationMapping
    public CustomResponseEntity updatePackage(@Argument PackageEntity packageEntity){
        return packageService.updatePackage(packageEntity);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @MutationMapping
    public CustomResponseEntity deletePackageById(@Argument Long id){
        return packageService.deletePackageById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @QueryMapping
    public PackageEntity findPackageByTrackingCode(@Argument String trackingCode){
        return packageService.findPackageByTrackingCode(trackingCode);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @QueryMapping
    public List<PackageEntity> findPackagesByDateRange(@Argument LocalDateTime startDate, @Argument LocalDateTime endDate){
        return packageService.findPackagesByDateRange(startDate, endDate);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @QueryMapping
    public PackageCountByUserDTO findPackageCountByUserId (@Argument Long userId) {
        return packageService.findPackageCountByUserId(userId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @QueryMapping
    public List<PackageEntity> findPackagesByStatusIn(@Argument List<Integer> packageStatuses) {
        return packageService.findPackagesByStatusIn(packageStatuses);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @QueryMapping
    public List<PackageCountByUserDTO> findPackageCountByAllUsers() {
        return packageService.findCountByAllUsers();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @QueryMapping
    public List<PackageEntity> findAllPackagesByUserId(@Argument Long userId) {
        return packageService.findAllPackagesByUserId(userId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS', 'WAREHOUSE', 'SUPERVISOR')")
    @QueryMapping
    public List<PackageEntity> findAllPackagesByUbication(@Argument String origin, @Argument String destination) {
        return packageService.findAllPackagesByUbication(origin, destination);
    }
}
