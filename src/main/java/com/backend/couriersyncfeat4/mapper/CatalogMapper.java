package com.backend.couriersyncfeat4.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.backend.couriersyncfeat4.dto.output.AlertTypeResponse;
import com.backend.couriersyncfeat4.dto.output.PackageStatusResponse;
import com.backend.couriersyncfeat4.dto.output.RoleResponse;
import com.backend.couriersyncfeat4.entity.AlertTypeEntity;
import com.backend.couriersyncfeat4.entity.PackageStatusEntity;
import com.backend.couriersyncfeat4.entity.RoleEntity;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CatalogMapper {

    RoleResponse toRoleResponse(RoleEntity role);

    PackageStatusResponse toPackageStatusResponse(PackageStatusEntity status);

    AlertTypeResponse toAlertTypeResponse(AlertTypeEntity alertType);
}
