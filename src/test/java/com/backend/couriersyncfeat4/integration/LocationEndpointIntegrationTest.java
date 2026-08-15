package com.backend.couriersyncfeat4.integration;

import org.junit.jupiter.api.Test;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LocationEndpointIntegrationTest extends IntegrationTestBase {

    private Map<String, Object> locationInput() {
        return Map.of("latitude", 4.6, "longitude", -74.0, "address", "Calle 80");
    }

    @Test
    @Operation("/graphql{addLocation}")
    void addAndFindLocation() {
        String pkg = createPackageUuid(adminToken);

        GraphQlTester.Response created = as(adminToken)
                .document("mutation($p: ID!, $i: LocationAddInput!){ addLocation(packageId: $p, input: $i){ id address } }")
                .variable("p", pkg).variable("i", locationInput()).execute();
        created.path("addLocation.id").entity(Object.class).satisfies(id -> assertThat(id).isNotNull());
        created.path("addLocation.address").entity(String.class).isEqualTo("Calle 80");

        as(adminToken)
                .document("query($p: ID!){ findAllLocationsByPackageId(packageId: $p){ id address } }")
                .variable("p", pkg).execute()
                .path("findAllLocationsByPackageId").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));

        as(adminToken)
                .document("query($p: ID!){ findLastLocationByPackageId(packageId: $p){ id } }")
                .variable("p", pkg).execute()
                .path("findLastLocationByPackageId.id").entity(Object.class).satisfies(id -> assertThat(id).isNotNull());

        as(adminToken)
                .document("query($u: ID!){ findAllLocationsByUserId(userId: $u){ id } }")
                .variable("u", adminId).execute()
                .path("findAllLocationsByUserId").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
    }

    @Test
    @Operation("/graphql{findAllLocations}")
    void findAllLocations() {
        as(adminToken)
                .document("mutation($p: ID!, $i: LocationAddInput!){ addLocation(packageId: $p, input: $i){ id } }")
                .variable("p", createPackageUuid(adminToken)).variable("i", locationInput()).execute();

        as(adminToken).document("query { findAllLocations { id } }").execute()
                .path("findAllLocations").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
    }

    @Test
    @Operation("/graphql{findLocationsByTrackingCode}")
    void findLocationsByTrackingCode() {
        String pkg = createPackageUuid(adminToken);
        String trackingCode = as(adminToken)
                .document("query($id: ID!){ findPackageById(id: $id){ trackingCode } }")
                .variable("id", pkg).execute()
                .path("findPackageById.trackingCode").entity(String.class).get();

        as(adminToken)
                .document("mutation($p: ID!, $i: LocationAddInput!){ addLocation(packageId: $p, input: $i){ id } }")
                .variable("p", pkg).variable("i", locationInput()).execute();

        as(adminToken)
                .document("query($t: String!){ findLocationsByTrackingCode(trackingCode: $t){ id address } }")
                .variable("t", trackingCode).execute()
                .path("findLocationsByTrackingCode").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
    }

    @Test
    @Operation("/graphql{addLocation}")
    void addLocationForbiddenForCustomer() {
        expectForbidden(as(custToken)
                .document("mutation($p: ID!, $i: LocationAddInput!){ addLocation(packageId: $p, input: $i){ id } }")
                .variable("p", "00000000-0000-0000-0000-000000000000").variable("i", locationInput()).execute().errors());
    }
}
