package ch.css.demo.caseflow.adapter.in.rest;

import ch.css.demo.caseflow.adapter.in.rest.security.Roles;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integrationstest von {@code POST /api/v1/cases}: Happy Path, Pflichtfeld-
 * Validierung und Rollenschutz gegen die Dev-Services-Datenbank.
 */
@QuarkusTest
class CaseResourceIT {

    @Test
    @TestSecurity(user = "anna.cm", roles = Roles.CASE_MANAGER)
    void createCase_legtFallImStatusNeuMitFallnummerAn() {
        given()
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
                .header("Location", notNullValue())
                .body("status", equalTo("NEU"))
                .body("case_number", matchesPattern("CASE-\\d{6}"))
                .body("case_reference", equalTo("KND-4711"))
                .body("created_at", notNullValue());
    }

    @Test
    @TestSecurity(user = "anna.cm", roles = Roles.CASE_MANAGER)
    void createCase_ohneFalltyp_wirdAbgelehntUndNenntFeld() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "priority", "HOCH",
                        "source", "TELEFON",
                        "case_reference", "KND-4711"))
        .when()
                .post("/api/v1/cases")
        .then()
                .statusCode(400)
                .body(containsString("Falltyp"));
    }

    @Test
    @TestSecurity(user = "rita.audit", roles = Roles.AUDITOR)
    void createCase_alsAuditor_wirdVerweigert() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "case_type", "LEISTUNG",
                        "priority", "HOCH",
                        "source", "TELEFON",
                        "case_reference", "KND-4711"))
        .when()
                .post("/api/v1/cases")
        .then()
                .statusCode(403);
    }
}
