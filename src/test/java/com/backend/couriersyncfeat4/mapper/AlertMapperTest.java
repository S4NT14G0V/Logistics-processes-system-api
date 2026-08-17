package com.backend.couriersyncfeat4.mapper;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.backend.couriersyncfeat4.dto.output.AlertResponse;
import com.backend.couriersyncfeat4.entity.AlertEntity;
import com.backend.couriersyncfeat4.entity.AlertTypeEntity;
import com.backend.couriersyncfeat4.entity.PackageEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = { AlertMapperImpl.class, CatalogMapperImpl.class })
class AlertMapperTest {

    @Autowired
    private AlertMapper alertMapper;

    @Test
    void shouldMapAlertToResponse() {
        AlertTypeEntity alertType = new AlertTypeEntity();
        alertType.setId(1);
        alertType.setName("Delayed");
        alertType.setDescription("Package delayed");

        PackageEntity pkg = new PackageEntity();
        pkg.setUuid(UUID.randomUUID());

        AlertEntity alert = new AlertEntity();
        alert.setId(10L);
        alert.setDescription("Paquete demorado");
        alert.setAlertTypeEntity(alertType);
        alert.setPackageEntity(pkg);

        AlertResponse response = alertMapper.toResponse(alert);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.description()).isEqualTo("Paquete demorado");
        assertThat(response.alertType()).isNotNull();
        assertThat(response.alertType().name()).isEqualTo("Delayed");
        assertThat(response.packageId()).isEqualTo(pkg.getUuid());
    }
}
