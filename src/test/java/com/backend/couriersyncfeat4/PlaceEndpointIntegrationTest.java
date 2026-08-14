package com.backend.couriersyncfeat4;

import org.junit.jupiter.api.Test;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceEndpointIntegrationTest extends IntegrationTestBase {

    @Test
    @Operation("/graphql{findAllPlaces}")
    void findAllPlaces() {
        as(custToken).document("query { findAllPlaces" + PLACE_FIELDS + " }").execute()
                .path("findAllPlaces").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
    }

    @Test
    @Operation("/graphql{findPlaceByUuid}")
    void findPlaceByUuid() {
        GraphQlTester.Response r = as(custToken)
                .document("query($u: ID!){ findPlaceByUuid(uuid: $u)" + PLACE_FIELDS + " }")
                .variable("u", originId).execute();
        assertPlaceShape(r, "findPlaceByUuid");
    }

    @Test
    @Operation("/graphql{createPlace}")
    void createPlace() {
        GraphQlTester.Response ok = as(adminToken)
                .document("mutation($i: PlaceInput!){ createPlace(input: $i)" + PLACE_FIELDS + " }")
                .variable("i", placeInput("X")).execute();
        assertPlaceShape(ok, "createPlace");

        expectForbidden(as(custToken).document("mutation($i: PlaceInput!){ createPlace(input: $i){ uuid } }")
                .variable("i", placeInput("Y")).execute().errors());

        // validación: name vacío
        as(adminToken).document("mutation($i: PlaceInput!){ createPlace(input: $i){ uuid } }")
                .variable("i", placeInput("")).execute()
                .errors().satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    @Operation("/graphql{updatePlace}")
    void updatePlace() {
        GraphQlTester.Response r = as(adminToken)
                .document("mutation($u: ID!, $i: PlaceInput!){ updatePlace(uuid: $u, input: $i)" + PLACE_FIELDS + " }")
                .variable("u", originId).variable("i", placeInput("Actualizado")).execute();
        assertPlaceShape(r, "updatePlace");
    }

    @Test
    @Operation("/graphql{deletePlace}")
    void deletePlace() {
        String p = createPlaceUuid(adminToken, "Del " + UUID.randomUUID(), 1.0, 1.0);
        as(adminToken).document("mutation($u: ID!){ deletePlace(uuid: $u) }")
                .variable("u", p).execute()
                .path("deletePlace").entity(Boolean.class).isEqualTo(true);
    }
}
