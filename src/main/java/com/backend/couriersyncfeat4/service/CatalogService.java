package com.backend.couriersyncfeat4.service;

import com.backend.couriersyncfeat4.dto.output.AlertTypeResponse;
import com.backend.couriersyncfeat4.dto.output.PackageStatusResponse;
import com.backend.couriersyncfeat4.dto.output.RoleResponse;
import com.backend.couriersyncfeat4.mapper.CatalogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {

    private final RoleService roleService;
    private final PackageStatusService packageStatusService;
    private final AlertTypeService alertTypeService;
    private final CatalogMapper catalogMapper;

    public CatalogService(RoleService roleService, PackageStatusService packageStatusService,
                          AlertTypeService alertTypeService, CatalogMapper catalogMapper) {
        this.roleService = roleService;
        this.packageStatusService = packageStatusService;
        this.alertTypeService = alertTypeService;
        this.catalogMapper = catalogMapper;
    }

    public List<RoleResponse> findAllRoles() {
        return roleService.findAll().stream().map(catalogMapper::toRoleResponse).toList();
    }

    public RoleResponse findRoleById(int id) {
        return catalogMapper.toRoleResponse(roleService.findById(id));
    }

    public List<PackageStatusResponse> findAllPackageStatuses() {
        return packageStatusService.findAll().stream().map(catalogMapper::toPackageStatusResponse).toList();
    }

    public PackageStatusResponse findPackageStatusById(int id) {
        return catalogMapper.toPackageStatusResponse(packageStatusService.findById(id));
    }

    public List<AlertTypeResponse> findAllAlertTypes() {
        return alertTypeService.findAll().stream().map(catalogMapper::toAlertTypeResponse).toList();
    }

    public AlertTypeResponse findAlertTypeById(int id) {
        return catalogMapper.toAlertTypeResponse(alertTypeService.findById(id));
    }
}
