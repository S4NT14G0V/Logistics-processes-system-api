package com.backend.couriersyncfeat4.mapper;

import org.junit.jupiter.api.Test;

import com.backend.couriersyncfeat4.dto.output.AlertTypeResponse;
import com.backend.couriersyncfeat4.dto.output.PackageStatusResponse;
import com.backend.couriersyncfeat4.dto.output.RoleResponse;
import com.backend.couriersyncfeat4.entity.AlertTypeEntity;
import com.backend.couriersyncfeat4.entity.PackageStatusEntity;
import com.backend.couriersyncfeat4.entity.RoleEntity;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogMapperTest {

    private final CatalogMapper catalogMapper = new CatalogMapperImpl();

    @Test
    void shouldMapRole() {
        RoleEntity role = new RoleEntity();
        role.setId(2);
        role.setName("LOGISTICS");
        role.setDescription("Logistics operator");

        RoleResponse response = catalogMapper.toRoleResponse(role);

        assertThat(response.id()).isEqualTo(2);
        assertThat(response.name()).isEqualTo("LOGISTICS");
        assertThat(response.description()).isEqualTo("Logistics operator");
    }

    @Test
    void shouldMapPackageStatus() {
        PackageStatusEntity status = new PackageStatusEntity();
        status.setId(3);
        status.setCode("IN_TRANSIT");
        status.setName("In Transit");
        status.setDescription("Package in transit");

        PackageStatusResponse response = catalogMapper.toPackageStatusResponse(status);

        assertThat(response.id()).isEqualTo(3);
        assertThat(response.code()).isEqualTo("IN_TRANSIT");
        assertThat(response.name()).isEqualTo("In Transit");
    }

    @Test
    void shouldMapAlertType() {
        AlertTypeEntity alertType = new AlertTypeEntity();
        alertType.setId(1);
        alertType.setName("Delayed");
        alertType.setDescription("Package delayed");

        AlertTypeResponse response = catalogMapper.toAlertTypeResponse(alertType);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("Delayed");
        assertThat(response.description()).isEqualTo("Package delayed");
    }
}
