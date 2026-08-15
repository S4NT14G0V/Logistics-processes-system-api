package com.backend.couriersyncfeat4.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.backend.couriersyncfeat4.dto.input.PackageInput;
import com.backend.couriersyncfeat4.dto.output.PackageResponse;
import com.backend.couriersyncfeat4.dto.output.PackageStatusResponse;
import com.backend.couriersyncfeat4.dto.output.PackageTrackingResponse;
import com.backend.couriersyncfeat4.entity.PackageEntity;
import com.backend.couriersyncfeat4.entity.PackageStatusEntity;
import com.backend.couriersyncfeat4.entity.PackageStatusHistoryEntity;
import com.backend.couriersyncfeat4.entity.PlaceEntity;
import com.backend.couriersyncfeat4.entity.UserEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = { PackageMapperImpl.class, PlaceMapperImpl.class })
class PackageMapperTest {

    @Autowired
    private PackageMapper packageMapper;

    private PlaceEntity origin;
    private PlaceEntity destination;

    @BeforeEach
    void setUp() {
        origin = place("Origen");
        destination = place("Destino");
    }

    @Test
    void shouldMapInputToEntity() {
        PackageInput input = new PackageInput("Libros", UUID.randomUUID(), UUID.randomUUID(),
                null, 2.5, 20.0, 15.0, 10.0, 100000.0);

        PackageEntity entity = packageMapper.toEntity(input, origin, destination);

        assertThat(entity).isNotNull();
        assertThat(entity.getUuid()).isNull();
        assertThat(entity.getTrackingCode()).isNull();
        assertThat(entity.getDescription()).isEqualTo("Libros");
        assertThat(entity.getWeightKg()).isEqualTo(2.5);
        assertThat(entity.getLengthCm()).isEqualTo(20.0);
        assertThat(entity.getWidthCm()).isEqualTo(15.0);
        assertThat(entity.getHeightCm()).isEqualTo(10.0);
        assertThat(entity.getDeclaredValue()).isEqualTo(100000.0);
        assertThat(entity.getOrigin()).isSameAs(origin);
        assertThat(entity.getDestination()).isSameAs(destination);
    }

    @Test
    void shouldMapEntityToResponse() {
        PackageEntity entity = packageEntity();
        PackageResponse response = packageMapper.toResponse(entity);

        assertThat(response).isNotNull();
        assertThat(response.uuid()).isEqualTo(entity.getUuid());
        assertThat(response.trackingCode()).isEqualTo("ABC-123");
        assertThat(response.status().code()).isEqualTo("CREATED");
        assertThat(response.origin().uuid()).isEqualTo(origin.getUuid());
        assertThat(response.destination().uuid()).isEqualTo(destination.getUuid());
        assertThat(response.history()).hasSize(1);
        assertThat(response.history().get(0).changedBy()).isEqualTo("admin@test.com");
        assertThat(response.history().get(0).toStatus().code()).isEqualTo("CREATED");
    }

    @Test
    void shouldMapEntityToTrackingResponseWithoutChangedBy() {
        PackageTrackingResponse response = packageMapper.toTrackingResponse(packageEntity());

        assertThat(response.trackingCode()).isEqualTo("ABC-123");
        assertThat(response.history()).hasSize(1);
        assertThat(response.history().get(0).changedBy()).isNull();
    }

    @Test
    void shouldMapStatusToResponse() {
        PackageStatusResponse status = packageMapper.toStatusResponse(
                new PackageStatusEntity(1, "CREATED", "Creado", "Paquete creado"));

        assertThat(status.id()).isEqualTo(1);
        assertThat(status.code()).isEqualTo("CREATED");
        assertThat(status.name()).isEqualTo("Creado");
        assertThat(status.description()).isEqualTo("Paquete creado");
    }

    @Test
    void shouldReturnEmptyHistoryWhenNull() {
        PackageEntity entity = packageEntity();
        entity.setStatusHistory(null);

        assertThat(packageMapper.toResponse(entity).history()).isEmpty();
    }

    private PackageEntity packageEntity() {
        PackageStatusEntity status = new PackageStatusEntity(1, "CREATED", "Creado", "Paquete creado");

        UserEntity changedBy = new UserEntity();
        changedBy.setEmail("admin@test.com");

        PackageStatusHistoryEntity history = new PackageStatusHistoryEntity();
        history.setChangedAt(LocalDateTime.now());
        history.setFromStatus(null);
        history.setToStatus(status);
        history.setChangedBy(changedBy);
        history.setDescription("Paquete creado");

        PackageEntity entity = new PackageEntity();
        entity.setUuid(UUID.randomUUID());
        entity.setTrackingCode("ABC-123");
        entity.setDescription("Libros");
        entity.setStatus(status);
        entity.setOrigin(origin);
        entity.setDestination(destination);
        entity.setStatusHistory(List.of(history));
        return entity;
    }

    private PlaceEntity place(String name) {
        PlaceEntity place = new PlaceEntity();
        place.setUuid(UUID.randomUUID());
        place.setName(name);
        place.setAddress("Calle 1");
        place.setCity("Bogotá");
        place.setDepartment("Cundinamarca");
        place.setLatitude(4.7110f);
        place.setLongitude(-74.0721f);
        return place;
    }
}
