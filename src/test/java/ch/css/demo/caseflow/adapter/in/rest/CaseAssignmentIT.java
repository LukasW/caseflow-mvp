package ch.css.demo.caseflow.adapter.in.rest;

import ch.css.demo.caseflow.adapter.in.rest.security.Roles;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Integrationstest von {@code PUT /api/v1/cases/{id}/assignment}: Zuweisung
 * durch {@code TEAM_LEAD}, Rollenschutz gegen {@code CASE_MANAGER} und
 * Behandlung unbekannter Fälle gegen die Dev-Services-Datenbank.
 */
@QuarkusTest
class CaseAssignmentIT {

    @Test
    @TestSecurity(user = "tom.tl", roles = { Roles.CASE_MANAGER, Roles.TEAM_LEAD })
    void assign_alsTeamLead_setztZustaendigkeit() {
        String caseId = createCase();

        given()
                .contentType("application/json")
                .body(Map.of("assignee", "bruno.cm"))
        .when()
                .put("/api/v1/cases/{id}/assignment", caseId)
        .then()
                .statusCode(200)
                .body("assignee", equalTo("bruno.cm"))
                .body("status", equalTo("NEU"));
    }

    @Test
    @TestSecurity(user = "anna.cm", roles = Roles.CASE_MANAGER)
    void assign_alsCaseManager_wirdVerweigert() {
        String caseId = createCase();

        given()
                .contentType("application/json")
                .body(Map.of("assignee", "bruno.cm"))
        .when()
                .put("/api/v1/cases/{id}/assignment", caseId)
        .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "tom.tl", roles = Roles.TEAM_LEAD)
    void assign_unbekannterFall_liefert404() {
        given()
                .contentType("application/json")
                .body(Map.of("assignee", "bruno.cm"))
        .when()
                .put("/api/v1/cases/{id}/assignment", UUID.randomUUID().toString())
        .then()
                .statusCode(404);
    }

    private static String createCase() {
        return given()
                .contentType("application/json")
                .body(Map.of(
                        "case_type", "LEISTUNG",
                        "priority", "HOCH",
                        "source", "TELEFON",
                        "case_reference", "KND-4711"))
        .when()
                .post("/api/v1/cases")
        .then()
                .statusCode(201)
                .extract().jsonPath().getString("id");
    }
}
