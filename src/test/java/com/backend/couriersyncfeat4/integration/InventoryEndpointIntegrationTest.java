package com.backend.couriersyncfeat4.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryEndpointIntegrationTest extends IntegrationTestBase {

    @Test
    @Operation("/graphql{inventorySummary}")
    void inventorySummary() {
        createPackageUuid(adminToken);
        as(adminToken).document("query { inventorySummary { region inTransit delivered pending } }").execute()
                .path("inventorySummary").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
    }

    @Test
    @Operation("/graphql{inventorySummary}")
    void inventorySummaryForbiddenForCustomer() {
        expectForbidden(as(custToken).document("query { inventorySummary { region } }").execute().errors());
    }
}
