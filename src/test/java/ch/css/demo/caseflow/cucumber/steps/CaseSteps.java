package ch.css.demo.caseflow.cucumber.steps;

import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.restassured.response.Response;
import jakarta.persistence.EntityManager;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

/**
 * Fachliche Steps für das Erfassen von Fällen. Nutzt {@link CommonSteps#authenticated()}
 * für die szenariobezogenen Auth-Header und prüft den Audit-Trail direkt gegen
 * die Persistenz, da noch kein Lese-Endpunkt existiert (US-9).
 */
public class CaseSteps {

    private final CommonSteps commonSteps;
    private final EntityManager entityManager;
    private Response lastResponse;

    public CaseSteps(CommonSteps commonSteps, EntityManager entityManager) {
        this.commonSteps = commonSteps;
        this.entityManager = entityManager;
    }

    @Wenn("ich einen Fall mit Falltyp {string}, Priorität {string}, Quelle {string} und Kundenreferenz {string} erfasse")
    public void ichErfasseEinenFall(String falltyp, String prioritaet, String quelle, String referenz) {
        Map<String, Object> body = new HashMap<>();
        body.put("case_type", falltyp);
        body.put("priority", prioritaet);
        body.put("source", quelle);
        body.put("case_reference", referenz);
        lastResponse = commonSteps.authenticated().body(body).post("/api/v1/cases");
    }

    @Wenn("ich einen Fall ohne Falltyp mit Priorität {string}, Quelle {string} und Kundenreferenz {string} erfasse")
    public void ichErfasseEinenFallOhneFalltyp(String prioritaet, String quelle, String referenz) {
        Map<String, Object> body = new HashMap<>();
        body.put("priority", prioritaet);
        body.put("source", quelle);
        body.put("case_reference", referenz);
        lastResponse = commonSteps.authenticated().body(body).post("/api/v1/cases");
    }

    @Dann("erhalte ich beim Erfassen den Status {int}")
    public void erhalteIchBeimErfassenDenStatus(int status) {
        assertThat(lastResponse.statusCode(), equalTo(status));
    }

    @Und("der Fall trägt eine Fallnummer und den Status {string}")
    public void derFallTraegtFallnummerUndStatus(String status) {
        assertThat(lastResponse.jsonPath().getString("case_number"), matchesPattern("CASE-\\d{6}"));
        assertThat(lastResponse.jsonPath().getString("status"), equalTo(status));
    }

    @Und("der Audit-Trail enthält den Eintrag {string}")
    public void derAuditTrailEnthaeltDenEintrag(String action) {
        String caseNumber = lastResponse.jsonPath().getString("case_number");
        long entries = QuarkusTransaction.requiringNew().call(() ->
                ((Number) entityManager.createNativeQuery(
                        "SELECT count(*) FROM case_audit_entry a "
                                + "JOIN cases c ON a.case_id = c.id "
                                + "WHERE c.case_number = :number AND a.action = :action")
                        .setParameter("number", caseNumber)
                        .setParameter("action", action)
                        .getSingleResult()).longValue());
        assertThat(entries, equalTo(1L));
    }

    @Und("der Audit-Eintrag nennt den Akteur {string} mit gesetztem Zeitpunkt")
    public void derAuditEintragNenntAkteurUndZeitpunkt(String actor) {
        String caseNumber = lastResponse.jsonPath().getString("case_number");
        long entries = QuarkusTransaction.requiringNew().call(() ->
                ((Number) entityManager.createNativeQuery(
                        "SELECT count(*) FROM case_audit_entry a "
                                + "JOIN cases c ON a.case_id = c.id "
                                + "WHERE c.case_number = :number AND a.actor = :actor "
                                + "AND a.occurred_at IS NOT NULL")
                        .setParameter("number", caseNumber)
                        .setParameter("actor", actor)
                        .getSingleResult()).longValue());
        assertThat(entries, equalTo(1L));
    }

    @Und("es wurde kein Fall mit der Kundenreferenz {string} angelegt")
    public void esWurdeKeinFallAngelegt(String referenz) {
        long cases = QuarkusTransaction.requiringNew().call(() ->
                ((Number) entityManager.createNativeQuery(
                        "SELECT count(*) FROM cases WHERE case_reference = :reference")
                        .setParameter("reference", referenz)
                        .getSingleResult()).longValue());
        assertThat(cases, equalTo(0L));
    }

    @Und("die Rückmeldung nennt das fehlende Pflichtfeld {string}")
    public void dieRueckmeldungNenntDasFehlendePflichtfeld(String feld) {
        assertThat(lastResponse.asString(), containsString(feld));
    }
}
