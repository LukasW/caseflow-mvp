package ch.css.demo.caseflow.cucumber.steps;

import ch.css.demo.caseflow.domain.model.Assignee;
import ch.css.demo.caseflow.domain.model.Case;
import ch.css.demo.caseflow.domain.model.CaseId;
import ch.css.demo.caseflow.domain.model.CaseReference;
import ch.css.demo.caseflow.domain.model.CaseType;
import ch.css.demo.caseflow.domain.model.Priority;
import ch.css.demo.caseflow.domain.model.Source;
import ch.css.demo.caseflow.domain.port.out.CaseRepository;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.restassured.response.Response;
import jakarta.persistence.EntityManager;

import java.time.Instant;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Fachliche Steps für das Zuweisen und Umverteilen von Fällen. Der Ausgangsfall
 * wird zur Isolation direkt über die Persistenz angelegt; die Zuweisung selbst
 * läuft über den REST-Endpunkt mit den Auth-Headern des Szenarios.
 */
public class CaseAssignmentSteps {

    private final CommonSteps commonSteps;
    private final CaseRepository caseRepository;
    private final EntityManager entityManager;
    private CaseId caseId;
    private Response lastResponse;

    public CaseAssignmentSteps(CommonSteps commonSteps, CaseRepository caseRepository,
            EntityManager entityManager) {
        this.commonSteps = commonSteps;
        this.caseRepository = caseRepository;
        this.entityManager = entityManager;
    }

    @Angenommen("ein erfasster Fall ohne Zuständigkeit")
    public void einErfassterFallOhneZustaendigkeit() {
        caseId = QuarkusTransaction.requiringNew()
                .call(() -> caseRepository.save(newCase()).id());
    }

    @Angenommen("ein Fall, der {string} zugewiesen ist")
    public void einFallDerZugewiesenIst(String assignee) {
        caseId = QuarkusTransaction.requiringNew().call(() -> {
            Case saved = caseRepository.save(newCase());
            Case assigned = saved.assignTo(Assignee.of(assignee), "tom.tl", Instant.now());
            return caseRepository.save(assigned).id();
        });
    }

    @Wenn("ich den Fall {string} zuweise")
    public void ichWeiseDenFallZu(String assignee) {
        lastResponse = commonSteps.authenticated()
                .body(Map.of("assignee", assignee))
                .put("/api/v1/cases/{id}/assignment", caseId.value().toString());
    }

    @Dann("erhalte ich bei der Zuweisung den Status {int}")
    public void erhalteIchBeiDerZuweisungDenStatus(int status) {
        assertThat(lastResponse.statusCode(), equalTo(status));
    }

    @Und("der Fall ist {string} zugewiesen")
    public void derFallIstZugewiesen(String assignee) {
        assertThat(lastResponse.jsonPath().getString("assignee"), equalTo(assignee));
    }

    @Und("der Audit-Trail enthält {string} mit Vorher {string} und Nachher {string}")
    public void derAuditTrailEnthaeltVorherNachher(String action, String previous, String next) {
        long entries = QuarkusTransaction.requiringNew().call(() ->
                ((Number) entityManager.createNativeQuery(
                        "SELECT count(*) FROM case_audit_entry a "
                                + "WHERE a.case_id = :caseId AND a.action = :action "
                                + "AND a.previous_assignee IS NOT DISTINCT FROM CAST(:previous AS varchar) "
                                + "AND a.new_assignee IS NOT DISTINCT FROM CAST(:next AS varchar)")
                        .setParameter("caseId", caseId.value())
                        .setParameter("action", action)
                        .setParameter("previous", previous.isBlank() ? null : previous)
                        .setParameter("next", next.isBlank() ? null : next)
                        .getSingleResult()).longValue());
        assertThat(entries, equalTo(1L));
    }

    @Und("die Zuständigkeit des Falls bleibt unverändert")
    public void dieZustaendigkeitBleibtUnveraendert() {
        long unassigned = QuarkusTransaction.requiringNew().call(() ->
                ((Number) entityManager.createNativeQuery(
                        "SELECT count(*) FROM cases WHERE id = :caseId AND assignee IS NULL")
                        .setParameter("caseId", caseId.value())
                        .getSingleResult()).longValue());
        assertThat(unassigned, equalTo(1L));
    }

    private Case newCase() {
        return Case.createNew(
                caseRepository.nextCaseNumber(),
                CaseReference.of("KND-4711"),
                CaseType.LEISTUNG,
                Priority.HOCH,
                Source.TELEFON,
                "anna.cm",
                Instant.now());
    }
}
