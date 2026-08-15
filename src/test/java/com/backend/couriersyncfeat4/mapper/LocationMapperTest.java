package com.backend.couriersyncfeat4.mapper;

import org.junit.jupiter.api.Test;

import com.backend.couriersyncfeat4.dto.output.LocationResponse;
import com.backend.couriersyncfeat4.entity.LocationEntity;

import static org.assertj.core.api.Assertions.assertThat;

class LocationMapperTest {

    private final LocationMapper locationMapper = new LocationMapperImpl();

    @Test
    void shouldMapLocationToResponse() {
        LocationEntity location = new LocationEntity();
        location.setId(1L);
        location.setLatitude(4.7110f);
        location.setLongitude(-74.0721f);
        location.setAddress("Calle 80");

        LocationResponse response = locationMapper.toResponse(location);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.latitude()).isEqualTo(4.7110f);
        assertThat(response.longitude()).isEqualTo(-74.0721f);
        assertThat(response.address()).isEqualTo("Calle 80");
    }
}
