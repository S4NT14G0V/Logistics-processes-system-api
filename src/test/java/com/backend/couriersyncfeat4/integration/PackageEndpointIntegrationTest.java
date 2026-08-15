package com.backend.couriersyncfeat4.integration;

import org.junit.jupiter.api.Test;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PackageEndpointIntegrationTest extends IntegrationTestBase {

    // ---------- Queries ----------

    @Test
    @Operation("/graphql{findAllPackages}")
    void findAllPackages() {
        propose(custToken);
        as(custToken).document("query { findAllPackages { uuid } }").execute()
                .path("findAllPackages").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
        as(adminToken).document("query { findAllPackages { uuid } }").execute()
                .path("findAllPackages").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
    }

    @Test
    @Operation("/graphql{findPackageById}")
    void findPackageById() {
        String own = propose(custToken).uuid();
        GraphQlTester.Response r = as(custToken)
                .document("query($id: ID!){ findPackageById(id: $id)" + PACKAGE_FIELDS + " }")
                .variable("id", own).execute();
        assertPackageShape(r, "findPackageById", "PROPOSED");

        String others = createPackageUuid(adminToken);
        expectForbidden(as(custToken).document("query($id: ID!){ findPackageById(id: $id){ uuid } }")
                .variable("id", others).execute().errors());

        // ID inválido -> BAD_REQUEST (INVALID_INPUT), no INTERNAL_ERROR
        expectErrorCode(as(custToken).document("query($id: ID!){ findPackageById(id: $id){ uuid } }")
                .variable("id", "not-a-uuid").execute().errors(), "INVALID_INPUT");
    }

    @Test
    @Operation("/graphql{findPackageByTrackingCode}")
    void findPackageByTrackingCode() {
        String tracking = propose(custToken).trackingCode();
        GraphQlTester.Response r = as(null)
                .document("query($t: String!){ findPackageByTrackingCode(trackingCode: $t)" + TRACKING_FIELDS + " }")
                .variable("t", tracking).execute();
        assertTrackingShape(r);

        // contrato limitado: uuid no existe en PackageTrackingResponse
        as(null).document("query($t: String!){ findPackageByTrackingCode(trackingCode: $t){ uuid } }")
                .variable("t", tracking).execute()
                .errors().satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    @Operation("/graphql{findPackageHistory}")
    void findPackageHistory() {
        String uuid = propose(custToken).uuid();
        GraphQlTester.Response r = as(custToken)
                .document("query($id: ID!){ findPackageHistory(packageId: $id){ changedAt fromStatus{ code } toStatus{ code } description } }")
                .variable("id", uuid).execute();
        r.path("findPackageHistory").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
        r.path("findPackageHistory[0].toStatus.code").entity(String.class).isEqualTo("PROPOSED");
        r.path("findPackageHistory[0].description").entity(String.class).satisfies(d -> assertThat(d).isNotBlank());
    }

    @Test
    @Operation("/graphql{findPackagesByDateRange}")
    void findPackagesByDateRange() {
        propose(custToken);
        as(custToken)
                .document("query { findPackagesByDateRange(startDate: \"2000-01-01T00:00:00\", endDate: \"2100-01-01T00:00:00\") { uuid } }")
                .execute()
                .path("findPackagesByDateRange").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
    }

    @Test
    @Operation("/graphql{findPackageCountByUserId}")
    void findPackageCountByUserId() {
        as(custToken).document("query($uid: ID!){ findPackageCountByUserId(userId: $uid){ packageCount } }")
                .variable("uid", custId).execute()
                .path("findPackageCountByUserId.packageCount").entity(Integer.class).satisfies(v -> assertThat(v).isNotNull());

        expectForbidden(as(custToken).document("query($uid: ID!){ findPackageCountByUserId(userId: $uid){ packageCount } }")
                .variable("uid", adminId).execute().errors());

        // userId inexistente -> USER_NOT_FOUND
        expectErrorCode(as(adminToken).document("query($uid: ID!){ findPackageCountByUserId(userId: $uid){ packageCount } }")
                .variable("uid", UUID.randomUUID().toString()).execute().errors(), "USER_NOT_FOUND");
    }

    @Test
    @Operation("/graphql{findPackagesByStatusIn}")
    void findPackagesByStatusIn() {
        propose(custToken);
        as(custToken).document("query($s: [String!]){ findPackagesByStatusIn(packageStatuses: $s){ uuid } }")
                .variable("s", List.of("PROPOSED")).execute()
                .path("findPackagesByStatusIn").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));

        as(custToken).document("query { findPackagesByStatusIn(packageStatuses: null){ uuid } }").execute()
                .path("findPackagesByStatusIn").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
    }

    @Test
    @Operation("/graphql{findPackageCountByAllUsers}")
    void findPackageCountByAllUsers() {
        GraphQlTester.Response r = as(adminToken)
                .document("query { findPackageCountByAllUsers { totalPackages users { userId packageCount } } }").execute();
        r.path("findPackageCountByAllUsers.totalPackages").entity(Integer.class).satisfies(v -> assertThat(v).isNotNull());
        r.path("findPackageCountByAllUsers.users").entityList(Object.class).satisfies(l -> assertThat(l).isNotNull());

        expectForbidden(as(custToken).document("query { findPackageCountByAllUsers { totalPackages } }").execute().errors());
    }

    @Test
    @Operation("/graphql{findPackageCountByAllStatus}")
    void findPackageCountByAllStatus() {
        as(adminToken).document("query { findPackageCountByAllStatus { statusCode count } }").execute()
                .path("findPackageCountByAllStatus").entityList(Object.class).satisfies(l -> assertThat(l).isNotNull());

        expectForbidden(as(custToken).document("query { findPackageCountByAllStatus { statusCode } }").execute().errors());
    }

    @Test
    @Operation("/graphql{findAllPackagesByUserId}")
    void findAllPackagesByUserId() {
        as(custToken).document("query($uid: ID!){ findAllPackagesByUserId(userId: $uid){ uuid } }")
                .variable("uid", custId).execute()
                .path("findAllPackagesByUserId").entityList(Object.class).satisfies(l -> assertThat(l).isNotNull());

        expectForbidden(as(custToken).document("query($uid: ID!){ findAllPackagesByUserId(userId: $uid){ uuid } }")
                .variable("uid", adminId).execute().errors());

        // userId inexistente -> USER_NOT_FOUND
        expectErrorCode(as(adminToken).document("query($uid: ID!){ findAllPackagesByUserId(userId: $uid){ uuid } }")
                .variable("uid", UUID.randomUUID().toString()).execute().errors(), "USER_NOT_FOUND");
    }

    @Test
    @Operation("/graphql{findAllPackagesByPlace}")
    void findAllPackagesByPlace() {
        propose(custToken);
        as(custToken).document("query($o: ID, $d: ID){ findAllPackagesByPlace(origin: $o, destination: $d){ uuid } }")
                .variable("o", originId).variable("d", destId).execute()
                .path("findAllPackagesByPlace").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
    }

    // ---------- Mutations ----------

    @Test
    @Operation("/graphql{createPackage}")
    void createPackage() {
        GraphQlTester.Response ok = as(adminToken)
                .document("mutation($i: PackageInput!){ createPackage(input: $i)" + PACKAGE_FIELDS + " }")
                .variable("i", baseInput()).execute();
        assertPackageShape(ok, "createPackage", "CREATED");

        expectForbidden(as(custToken).document("mutation($i: PackageInput!){ createPackage(input: $i){ uuid } }")
                .variable("i", baseInput()).execute().errors());

        // validación: peso negativo
        Map<String, Object> bad = baseInput();
        bad.put("weightKg", -5.0);
        as(adminToken).document("mutation($i: PackageInput!){ createPackage(input: $i){ uuid } }")
                .variable("i", bad).execute()
                .errors().satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    @Operation("/graphql{proposePackage}")
    void proposePackage() {
        GraphQlTester.Response ok = as(custToken)
                .document("mutation($i: PackageInput!){ proposePackage(input: $i)" + PACKAGE_FIELDS + " }")
                .variable("i", baseInput()).execute();
        assertPackageShape(ok, "proposePackage", "PROPOSED");

        expectForbidden(as(adminToken).document("mutation($i: PackageInput!){ proposePackage(input: $i){ uuid } }")
                .variable("i", baseInput()).execute().errors());

        // validación: dimensión inválida
        Map<String, Object> bad = baseInput();
        bad.put("lengthCm", 0);
        as(custToken).document("mutation($i: PackageInput!){ proposePackage(input: $i){ uuid } }")
                .variable("i", bad).execute()
                .errors().satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    @Operation("/graphql{approvePackage}")
    void approvePackage() {
        String uuid = propose(custToken).uuid();
        GraphQlTester.Response r = as(adminToken)
                .document("mutation($id: ID!){ approvePackage(id: $id)" + PACKAGE_FIELDS + " }")
                .variable("id", uuid).execute();
        assertPackageShape(r, "approvePackage", "CREATED");
    }

    @Test
    @Operation("/graphql{rejectPackage}")
    void rejectPackage() {
        String uuid = propose(custToken).uuid();
        GraphQlTester.Response r = as(adminToken)
                .document("mutation($id: ID!, $r: String){ rejectPackage(id: $id, reason: $r)" + PACKAGE_FIELDS + " }")
                .variable("id", uuid).variable("r", "no").execute();
        assertPackageShape(r, "rejectPackage", "CANCELLED");
    }

    @Test
    @Operation("/graphql{reactivatePackage}")
    void reactivatePackage() {
        String uuid = propose(custToken).uuid();
        as(adminToken).document("mutation($id: ID!, $r: String){ rejectPackage(id: $id, reason: $r){ uuid } }")
                .variable("id", uuid).variable("r", "no").execute();
        GraphQlTester.Response r = as(adminToken)
                .document("mutation($id: ID!){ reactivatePackage(id: $id)" + PACKAGE_FIELDS + " }")
                .variable("id", uuid).execute();
        assertPackageShape(r, "reactivatePackage", "CREATED");
    }

    @Test
    @Operation("/graphql{updatePackage}")
    void updatePackage() {
        // customer en PROPOSED -> error de negocio
        String proposed = propose(custToken).uuid();
        expectErrorCode(as(custToken).document("mutation($id: ID!, $i: PackageUpdateInput!){ updatePackage(id: $id, input: $i){ uuid } }")
                .variable("id", proposed).variable("i", Map.of("description", "x")).execute().errors(), "PACKAGE_NOT_UPDATABLE");

        // customer sobre paquete ajeno -> Forbidden
        String others = createPackageUuid(adminToken);
        expectForbidden(as(custToken).document("mutation($id: ID!, $i: PackageUpdateInput!){ updatePackage(id: $id, input: $i){ uuid } }")
                .variable("id", others).variable("i", Map.of("description", "x")).execute().errors());

        // admin en CREATED -> ok
        GraphQlTester.Response ok = as(adminToken)
                .document("mutation($id: ID!, $i: PackageUpdateInput!){ updatePackage(id: $id, input: $i)" + PACKAGE_FIELDS + " }")
                .variable("id", others).variable("i", Map.of("description", "editado")).execute();
        assertPackageShape(ok, "updatePackage", "CREATED");
    }

    @Test
    @Operation("/graphql{cancelPackage}")
    void cancelPackage() {
        String uuid = createPackageUuid(adminToken);
        GraphQlTester.Response ok = as(adminToken)
                .document("mutation($id: ID!, $r: String){ cancelPackage(id: $id, reason: $r)" + PACKAGE_FIELDS + " }")
                .variable("id", uuid).variable("r", "x").execute();
        assertPackageShape(ok, "cancelPackage", "CANCELLED");

        String delivered = createPackageUuid(adminToken);
        as(adminToken).document("mutation($id: ID!, $c: String!){ changePackageStatus(id: $id, statusCode: $c){ uuid } }")
                .variable("id", delivered).variable("c", "IN_TRANSIT").execute();
        as(adminToken).document("mutation($id: ID!, $c: String!){ changePackageStatus(id: $id, statusCode: $c){ uuid } }")
                .variable("id", delivered).variable("c", "DELIVERED").execute();
        expectErrorCode(as(adminToken).document("mutation($id: ID!, $r: String){ cancelPackage(id: $id, reason: $r){ uuid } }")
                .variable("id", delivered).variable("r", "x").execute().errors(), "PACKAGE_NOT_CANCELLABLE");
    }

    @Test
    @Operation("/graphql{changePackageStatus}")
    void changePackageStatus() {
        String uuid = createPackageUuid(adminToken);
        GraphQlTester.Response r = as(adminToken)
                .document("mutation($id: ID!, $c: String!){ changePackageStatus(id: $id, statusCode: $c)" + PACKAGE_FIELDS + " }")
                .variable("id", uuid).variable("c", "IN_TRANSIT").execute();
        assertPackageShape(r, "changePackageStatus", "IN_TRANSIT");

        expectForbidden(as(custToken).document("mutation($id: ID!, $c: String!){ changePackageStatus(id: $id, statusCode: $c){ uuid } }")
                .variable("id", uuid).variable("c", "IN_TRANSIT").execute().errors());
    }

    @Test
    @Operation("/graphql{fullLifecycle}")
    void fullLifecycle() {
        String uuid = propose(custToken).uuid();

        as(adminToken).document("mutation($id: ID!){ approvePackage(id: $id){ status{ code } } }")
                .variable("id", uuid).execute()
                .path("approvePackage.status.code").entity(String.class).isEqualTo("CREATED");

        as(adminToken).document("mutation($id: ID!, $c: String!){ changePackageStatus(id: $id, statusCode: $c){ status{ code } } }")
                .variable("id", uuid).variable("c", "IN_TRANSIT").execute()
                .path("changePackageStatus.status.code").entity(String.class).isEqualTo("IN_TRANSIT");

        as(adminToken).document("mutation($id: ID!, $c: String!){ changePackageStatus(id: $id, statusCode: $c){ status{ code } } }")
                .variable("id", uuid).variable("c", "DELIVERED").execute()
                .path("changePackageStatus.status.code").entity(String.class).isEqualTo("DELIVERED");
    }
}
