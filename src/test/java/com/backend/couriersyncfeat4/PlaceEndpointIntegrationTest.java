package com.backend.couriersyncfeat4;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceEndpointIntegrationTest extends IntegrationTestBase {

    @Test
    void findAllPlaces() {
        assertThat(errors(graphql(custToken, "query { findAllPlaces { uuid } }", null))).isFalse();
    }

    @Test
    void findPlaceByUuid() {
        assertThat(errors(graphql(custToken, "query($u: ID!){ findPlaceByUuid(uuid: $u){ uuid } }", Map.of("u", originId)))).isFalse();
    }

    @Test
    void createPlace() {
        assertThat(errors(graphql(adminToken, "mutation($i: PlaceInput!){ createPlace(input: $i){ uuid } }", Map.of("i", placeInput("X"))))).isFalse();
        assertThat(errors(graphql(custToken, "mutation($i: PlaceInput!){ createPlace(input: $i){ uuid } }", Map.of("i", placeInput("Y"))))).isTrue();
    }

    @Test
    void updatePlace() {
        assertThat(errors(graphql(adminToken, "mutation($u: ID!, $i: PlaceInput!){ updatePlace(uuid: $u, input: $i){ uuid } }", Map.of("u", originId, "i", placeInput("Actualizado"))))).isFalse();
    }

    @Test
    void deletePlace() {
        String p = createPlace(adminToken, "Del " + UUID.randomUUID(), 1.0, 1.0);
        assertThat(errors(graphql(adminToken, "mutation($u: ID!){ deletePlace(uuid: $u) }", Map.of("u", p)))).isFalse();
    }
}
