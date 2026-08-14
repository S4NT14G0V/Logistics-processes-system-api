package com.backend.couriersyncfeat4;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PackageEndpointIntegrationTest extends IntegrationTestBase {

    // ---------- Queries ----------

    @Test
    void findAllPackages() {
        propose(custToken);
        assertThat(errors(graphql(custToken, "query { findAllPackages { uuid } }", null))).isFalse();
        assertThat(errors(graphql(adminToken, "query { findAllPackages { uuid } }", null))).isFalse();
    }

    @Test
    void findPackageById() {
        String own = propose(custToken).get("uuid").asText();
        assertThat(errors(graphql(custToken, "query($id: ID!){ findPackageById(id: $id){ uuid } }", Map.of("id", own)))).isFalse();

        String others = createPackage(adminToken).get("uuid").asText();
        assertThat(errors(graphql(custToken, "query($id: ID!){ findPackageById(id: $id){ uuid } }", Map.of("id", others)))).isTrue();
    }

    @Test
    void findPackageByTrackingCode() {
        String tracking = propose(custToken).get("trackingCode").asText();
        JsonNode r = graphql(null, "query($t: String!){ findPackageByTrackingCode(trackingCode: $t){ trackingCode status{code} } }", Map.of("t", tracking));
        assertThat(errors(r)).isFalse();
        assertThat(r.get("data").get("findPackageByTrackingCode").get("trackingCode").asText()).isEqualTo(tracking);
    }

    @Test
    void findPackageHistory() {
        String uuid = propose(custToken).get("uuid").asText();
        assertThat(errors(graphql(custToken, "query($id: ID!){ findPackageHistory(packageId: $id){ changedAt toStatus{code} } }", Map.of("id", uuid)))).isFalse();
    }

    @Test
    void findPackagesByDateRange() {
        propose(custToken);
        assertThat(errors(graphql(custToken, "query { findPackagesByDateRange(startDate: \"2000-01-01T00:00:00\", endDate: \"2100-01-01T00:00:00\") { uuid } }", null))).isFalse();
    }

    @Test
    void findPackageCountByUserId() {
        assertThat(errors(graphql(custToken, "query($uid: ID!){ findPackageCountByUserId(userId: $uid){ packageCount } }", Map.of("uid", custId)))).isFalse();
        assertThat(errors(graphql(custToken, "query($uid: ID!){ findPackageCountByUserId(userId: $uid){ packageCount } }", Map.of("uid", adminId)))).isTrue();
    }

    @Test
    void findPackagesByStatusIn() {
        propose(custToken);
        assertThat(errors(graphql(custToken, "query($s: [String!]){ findPackagesByStatusIn(packageStatuses: $s){ uuid } }", Map.of("s", List.of("PROPOSED"))))).isFalse();
        assertThat(errors(graphql(custToken, "query { findPackagesByStatusIn(packageStatuses: null){ uuid } }", null))).isFalse();
    }

    @Test
    void findPackageCountByAllUsers() {
        assertThat(errors(graphql(adminToken, "query { findPackageCountByAllUsers { totalPackages users { userId packageCount } } }", null))).isFalse();
        assertThat(errors(graphql(custToken, "query { findPackageCountByAllUsers { totalPackages } }", null))).isTrue();
    }

    @Test
    void findPackageCountByAllStatus() {
        assertThat(errors(graphql(adminToken, "query { findPackageCountByAllStatus { statusCode count } }", null))).isFalse();
        assertThat(errors(graphql(custToken, "query { findPackageCountByAllStatus { statusCode } }", null))).isTrue();
    }

    @Test
    void findAllPackagesByUserId() {
        assertThat(errors(graphql(custToken, "query($uid: ID!){ findAllPackagesByUserId(userId: $uid){ uuid } }", Map.of("uid", custId)))).isFalse();
        assertThat(errors(graphql(custToken, "query($uid: ID!){ findAllPackagesByUserId(userId: $uid){ uuid } }", Map.of("uid", adminId)))).isTrue();
    }

    @Test
    void findAllPackagesByPlace() {
        propose(custToken);
        assertThat(errors(graphql(custToken, "query($o: ID, $d: ID){ findAllPackagesByPlace(origin: $o, destination: $d){ uuid } }", Map.of("o", originId, "d", destId)))).isFalse();
    }

    // ---------- Mutations ----------

    @Test
    void createPackage() {
        assertThat(errors(graphql(adminToken, "mutation($i: PackageInput!){ createPackage(input: $i){ uuid } }", Map.of("i", baseInput())))).isFalse();
        assertThat(errors(graphql(custToken, "mutation($i: PackageInput!){ createPackage(input: $i){ uuid } }", Map.of("i", baseInput())))).isTrue();
    }

    @Test
    void proposePackage() {
        assertThat(errors(graphql(custToken, "mutation($i: PackageInput!){ proposePackage(input: $i){ uuid } }", Map.of("i", baseInput())))).isFalse();
        assertThat(errors(graphql(adminToken, "mutation($i: PackageInput!){ proposePackage(input: $i){ uuid } }", Map.of("i", baseInput())))).isTrue();
    }

    @Test
    void approvePackage() {
        String uuid = propose(custToken).get("uuid").asText();
        JsonNode r = graphql(adminToken, "mutation($id: ID!){ approvePackage(id: $id){ status{code} } }", Map.of("id", uuid));
        assertThat(r.get("data").get("approvePackage").get("status").get("code").asText()).isEqualTo("CREATED");
    }

    @Test
    void rejectPackage() {
        String uuid = propose(custToken).get("uuid").asText();
        JsonNode r = graphql(adminToken, "mutation($id: ID!, $r: String){ rejectPackage(id: $id, reason: $r){ status{code} } }", Map.of("id", uuid, "r", "no"));
        assertThat(r.get("data").get("rejectPackage").get("status").get("code").asText()).isEqualTo("CANCELLED");
    }

    @Test
    void reactivatePackage() {
        String uuid = propose(custToken).get("uuid").asText();
        graphql(adminToken, "mutation($id: ID!, $r: String){ rejectPackage(id: $id, reason: $r){ status{code} } }", Map.of("id", uuid, "r", "no"));
        JsonNode r = graphql(adminToken, "mutation($id: ID!){ reactivatePackage(id: $id){ status{code} } }", Map.of("id", uuid));
        assertThat(r.get("data").get("reactivatePackage").get("status").get("code").asText()).isEqualTo("CREATED");
    }

    @Test
    void updatePackage() {
        // customer en PROPOSED -> error de negocio
        String proposed = propose(custToken).get("uuid").asText();
        assertThat(errors(graphql(custToken, "mutation($id: ID!, $i: PackageUpdateInput!){ updatePackage(id: $id, input: $i){ uuid } }", Map.of("id", proposed, "i", Map.of("description", "x"))))).isTrue();

        // customer sobre paquete ajeno -> Forbidden
        String others = createPackage(adminToken).get("uuid").asText();
        assertThat(errors(graphql(custToken, "mutation($id: ID!, $i: PackageUpdateInput!){ updatePackage(id: $id, input: $i){ uuid } }", Map.of("id", others, "i", Map.of("description", "x"))))).isTrue();

        // admin en CREATED -> ok
        assertThat(errors(graphql(adminToken, "mutation($id: ID!, $i: PackageUpdateInput!){ updatePackage(id: $id, input: $i){ uuid } }", Map.of("id", others, "i", Map.of("description", "editado"))))).isFalse();
    }

    @Test
    void cancelPackage() {
        String uuid = createPackage(adminToken).get("uuid").asText();
        JsonNode r = graphql(adminToken, "mutation($id: ID!, $r: String){ cancelPackage(id: $id, reason: $r){ status{code} } }", Map.of("id", uuid, "r", "x"));
        assertThat(r.get("data").get("cancelPackage").get("status").get("code").asText()).isEqualTo("CANCELLED");

        String delivered = createPackage(adminToken).get("uuid").asText();
        graphql(adminToken, "mutation($id: ID!, $c: String!){ changePackageStatus(id: $id, statusCode: $c){ status{code} } }", Map.of("id", delivered, "c", "IN_TRANSIT"));
        graphql(adminToken, "mutation($id: ID!, $c: String!){ changePackageStatus(id: $id, statusCode: $c){ status{code} } }", Map.of("id", delivered, "c", "DELIVERED"));
        assertThat(errors(graphql(adminToken, "mutation($id: ID!, $r: String){ cancelPackage(id: $id, reason: $r){ uuid } }", Map.of("id", delivered, "r", "x")))).isTrue();
    }

    @Test
    void changePackageStatus() {
        String uuid = createPackage(adminToken).get("uuid").asText();
        JsonNode r = graphql(adminToken, "mutation($id: ID!, $c: String!){ changePackageStatus(id: $id, statusCode: $c){ status{code} } }", Map.of("id", uuid, "c", "IN_TRANSIT"));
        assertThat(r.get("data").get("changePackageStatus").get("status").get("code").asText()).isEqualTo("IN_TRANSIT");
        assertThat(errors(graphql(custToken, "mutation($id: ID!, $c: String!){ changePackageStatus(id: $id, statusCode: $c){ uuid } }", Map.of("id", uuid, "c", "IN_TRANSIT")))).isTrue();
    }
}
