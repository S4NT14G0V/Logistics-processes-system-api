package com.backend.couriersyncfeat4.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserEndpointIntegrationTest extends IntegrationTestBase {

    private static final String TEMP_PASSWORD = "TempPass123!";
    private static final String NEW_PASSWORD = "NewPass456!";

    private Map<String, Object> userInput(String email) {
        return Map.of("name", "Nuevo Usuario", "email", email, "roleId", 5, "temporaryPassword", TEMP_PASSWORD);
    }

    @Test
    @Operation("/graphql{createUser}")
    void createUserWithTemporaryPasswordAndChangePassword() {
        String email = "user-" + UUID.randomUUID() + "@test.com";

        GraphQlTester.Response created = as(adminToken)
                .document("mutation($i: UserInput!){ createUser(input: $i){ id name email } }")
                .variable("i", userInput(email)).execute();
        created.path("createUser.email").entity(String.class).isEqualTo(email);
        created.path("createUser.id").entity(String.class).satisfies(id -> assertThat(id).isNotBlank());

        JsonNode loginTemp = login(email, TEMP_PASSWORD);
        assertThat(loginTemp.get("changePasswordRequired").asBoolean()).isTrue();
        String tempToken = loginTemp.get("accessToken").asText();

        int status = postStatus("/auth/change-password",
                Map.of("currentPassword", TEMP_PASSWORD, "newPassword", NEW_PASSWORD), tempToken);
        assertThat(status).isEqualTo(204);

        JsonNode loginNew = login(email, NEW_PASSWORD);
        assertThat(loginNew.get("changePasswordRequired").asBoolean()).isFalse();
    }

    @Test
    @Operation("/graphql{findAllUsers}")
    void findAllUsers() {
        as(adminToken).document("query { findAllUsers { id name email } }").execute()
                .path("findAllUsers").entityList(Object.class).satisfies(l -> assertThat(l).hasSizeGreaterThan(0));
    }

    @Test
    @Operation("/graphql{findUserById}")
    void findUserById() {
        as(adminToken).document("query($id: ID!){ findUserById(id: $id){ id email } }")
                .variable("id", adminId).execute()
                .path("findUserById.email").entity(String.class).isEqualTo(ADMIN_EMAIL);
    }

    @Test
    @Operation("/graphql{deleteUser}")
    void deleteOwnAccountForbidden() {
        expectErrorCode(as(adminToken).document("mutation($id: ID!){ deleteUser(id: $id) }")
                .variable("id", adminId).execute().errors(), "FORBIDDEN");
    }

    @Test
    @Operation("/graphql{findAllUsers}")
    void findAllUsersForbiddenForCustomer() {
        expectForbidden(as(custToken).document("query { findAllUsers { id } }").execute().errors());
    }
}
