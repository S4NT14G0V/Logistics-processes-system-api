package com.backend.couriersyncfeat4.mapper;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.backend.couriersyncfeat4.dto.output.PlaceResponse;
import com.backend.couriersyncfeat4.entity.PlaceEntity;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceMapperTest {

    private final PlaceMapper placeMapper = new PlaceMapperImpl();

    @Test
    void shouldMapPlaceToResponse() {
        PlaceEntity place = new PlaceEntity();
        place.setUuid(UUID.randomUUID());
        place.setName("Bodega Central");
        place.setAddress("Calle 1 # 2-3");
        place.setCity("Bogotá");
        place.setDepartment("Cundinamarca");
        place.setLatitude(4.7110f);
        place.setLongitude(-74.0721f);

        PlaceResponse response = placeMapper.toResponse(place);

        assertThat(response).isNotNull();
        assertThat(response.uuid()).isEqualTo(place.getUuid());
        assertThat(response.name()).isEqualTo("Bodega Central");
        assertThat(response.address()).isEqualTo("Calle 1 # 2-3");
        assertThat(response.city()).isEqualTo("Bogotá");
        assertThat(response.department()).isEqualTo("Cundinamarca");
        assertThat(response.latitude()).isEqualTo(4.7110f);
        assertThat(response.longitude()).isEqualTo(-74.0721f);
    }
}
