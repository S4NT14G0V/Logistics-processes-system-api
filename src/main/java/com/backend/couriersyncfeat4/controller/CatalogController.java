package com.backend.couriersyncfeat4.controller;

import com.backend.couriersyncfeat4.dto.output.AlertTypeResponse;
import com.backend.couriersyncfeat4.dto.output.PackageStatusResponse;
import com.backend.couriersyncfeat4.dto.output.RoleResponse;
import com.backend.couriersyncfeat4.service.CatalogService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PreAuthorize("hasAuthority('catalog:read')")
    @QueryMapping
    public List<RoleResponse> findAllRoles() {
        return catalogService.findAllRoles();
    }

    @PreAuthorize("hasAuthority('catalog:read')")
    @QueryMapping
    public RoleResponse findRoleById(@Argument int id) {
        return catalogService.findRoleById(id);
    }

    @PreAuthorize("hasAuthority('catalog:read')")
    @QueryMapping
    public List<PackageStatusResponse> findAllPackagesStatus() {
        return catalogService.findAllPackageStatuses();
    }

    @PreAuthorize("hasAuthority('catalog:read')")
    @QueryMapping
    public PackageStatusResponse findPackageStatusById(@Argument int id) {
        return catalogService.findPackageStatusById(id);
    }

    @PreAuthorize("hasAuthority('catalog:read')")
    @QueryMapping
    public List<AlertTypeResponse> findAllAlertTypes() {
        return catalogService.findAllAlertTypes();
    }

    @PreAuthorize("hasAuthority('catalog:read')")
    @QueryMapping
    public AlertTypeResponse findAlertTypeById(@Argument int id) {
        return catalogService.findAlertTypeById(id);
    }
}
