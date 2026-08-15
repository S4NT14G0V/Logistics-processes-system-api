package com.backend.couriersyncfeat4.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogEndpointIntegrationTest extends IntegrationTestBase {

    @Test
    @Operation("/graphql{findAllRoles}")
    void findAllRoles() {
        as(adminToken).document("query { findAllRoles { id name description } }").execute()
                .path("findAllRoles").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
    }

    @Test
    @Operation("/graphql{findRoleById}")
    void findRoleById() {
        as(adminToken).document("query { findRoleById(id: 1) { id name } }").execute()
                .path("findRoleById.name").entity(String.class).isEqualTo("ADMIN");
    }

    @Test
    @Operation("/graphql{findAllPackagesStatus}")
    void findAllPackagesStatus() {
        as(adminToken).document("query { findAllPackagesStatus { id code name } }").execute()
                .path("findAllPackagesStatus").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
    }

    @Test
    @Operation("/graphql{findAllAlertTypes}")
    void findAllAlertTypes() {
        as(adminToken).document("query { findAllAlertTypes { id name } }").execute()
                .path("findAllAlertTypes").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
    }

    @Test
    @Operation("/graphql{findAllRoles}")
    void findAllRolesForbiddenForCustomer() {
        expectForbidden(as(custToken).document("query { findAllRoles { id } }").execute().errors());
    }
}
