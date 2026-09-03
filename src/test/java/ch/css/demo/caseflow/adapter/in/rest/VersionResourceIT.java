package ch.css.demo.caseflow.adapter.in.rest;

import ch.css.demo.caseflow.adapter.in.rest.security.Roles;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integrationstest des Endpunkts {@code /api/v1/version}: anonyme Requests
 * werden abgewiesen, angemeldete Nutzer erhalten die Build-Informationen.
 */
@QuarkusTest
class VersionResourceIT {

    @Test
    void getVersion_weistAnonymeRequestsAb() {
        given()
        .when()
                .get("/api/v1/version")
        .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "anna", roles = Roles.CASE_MANAGER)
    void getVersion_liefertBuildInformationen() {
        given()
        .when()
                .get("/api/v1/version")
        .then()
                .statusCode(200)
                .body("version", notNullValue())
                .body("build_timestamp", notNullValue())
                .body("git_commit_short", notNullValue());
    }
}
