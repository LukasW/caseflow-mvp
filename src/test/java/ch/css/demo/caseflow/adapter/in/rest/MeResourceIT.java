package ch.css.demo.caseflow.adapter.in.rest;

import ch.css.demo.caseflow.adapter.in.rest.security.Roles;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

/**
 * Integrationstest des Endpunkts {@code /api/v1/me}: prüft die Dev-Ersatz-
 * identität bei deaktiviertem OIDC sowie die Rückgabe der echten Identität
 * samt gefilterter CaseFlow-Rollen.
 */
@QuarkusTest
class MeResourceIT {

    @Test
    void getMe_liefertDevErsatzidentitaetWennNichtAngemeldet() {
        given()
        .when()
                .get("/api/v1/me")
        .then()
                .statusCode(200)
                .body("username", equalTo("dev"))
                .body("authenticated", equalTo(false))
                .body("roles", hasItem("CASE_MANAGER"));
    }

    @Test
    @TestSecurity(user = "anna.lead", roles = Roles.TEAM_LEAD)
    void getMe_liefertAngemeldetenNutzerMitRollen() {
        given()
        .when()
                .get("/api/v1/me")
        .then()
                .statusCode(200)
                .body("username", equalTo("anna.lead"))
                .body("authenticated", equalTo(true))
                .body("roles", contains("TEAM_LEAD"));
    }

    @Test
    @TestSecurity(user = "bob", roles = {Roles.CASE_MANAGER, "offline_access", "uma_authorization"})
    void getMe_filtertUnbekannteKeycloakRollenAus() {
        given()
        .when()
                .get("/api/v1/me")
        .then()
                .statusCode(200)
                .body("roles", contains("CASE_MANAGER"));
    }
}
