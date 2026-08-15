package com.backend.couriersyncfeat4.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertEndpointIntegrationTest extends IntegrationTestBase {

    @Test
    @Operation("/graphql{sendAlertToUser}")
    void sendAndListAlerts() {
        String pkg = createPackageUuid(adminToken);

        as(adminToken)
                .document("mutation($u: ID!, $p: ID!, $t: Int!, $d: String){ sendAlertToUser(userId: $u, packageId: $p, alertTypeId: $t, description: $d) }")
                .variable("u", custId).variable("p", pkg).variable("t", 1).variable("d", "Paquete demorado")
                .execute();

        as(adminToken).document("query { findAllAlerts { id description alertType{ name } packageId } }").execute()
                .path("findAllAlerts").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));

        as(custToken)
                .document("query($u: ID!){ findAllAlertsByUserId(userId: $u){ id description } }")
                .variable("u", custId).execute()
                .path("findAllAlertsByUserId").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
    }

    @Test
    @Operation("/graphql{findAllAlerts}")
    void findAllAlertsForbiddenForCustomer() {
        expectForbidden(as(custToken).document("query { findAllAlerts { id } }").execute().errors());
    }
}
