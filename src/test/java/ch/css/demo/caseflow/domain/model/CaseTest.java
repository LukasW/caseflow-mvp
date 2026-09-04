package ch.css.demo.caseflow.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prüft die Invarianten des {@link Case}-Aggregats und seiner Value Objects.
 */
class CaseTest {

    private static final Instant NOW = Instant.parse("2026-09-04T10:15:30Z");

    @Test
    void createNew_startetImStatusNeu() {
        Case newCase = createSampleCase();

        assertEquals(CaseStatus.NEU, newCase.status());
    }

    @Test
    void createNew_legtAuditEintragCaseCreatedAn() {
        Case newCase = createSampleCase();

        assertEquals(1, newCase.auditTrail().size());
        AuditEntry entry = newCase.auditTrail().getFirst();
        assertEquals(AuditAction.CASE_CREATED, entry.action());
        assertEquals("anna.cm", entry.actor());
        assertEquals(NOW, entry.timestamp());
    }

    @Test
    void auditTrail_istUnveraenderlich() {
        Case newCase = createSampleCase();

        assertThrows(UnsupportedOperationException.class,
                () -> newCase.auditTrail().add(AuditEntry.of(AuditAction.CASE_CREATED, "x", NOW)));
    }

    @Test
    void caseReference_darfNichtLeerSein() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new CaseReference("  "));
        assertTrue(ex.getMessage().contains("Kundenreferenz"));
    }

    @Test
    void caseNumber_erzwingtFormat() {
        assertThrows(IllegalArgumentException.class, () -> CaseNumber.of("42"));
    }

    @Test
    void assignTo_setztZustaendigkeitUndLiefertNeueInstanz() {
        Case newCase = createSampleCase();

        Case assigned = newCase.assignTo(Assignee.of("bruno.cm"), "tom.tl", NOW);

        assertEquals(Assignee.of("bruno.cm"), assigned.assignee());
        assertNull(newCase.assignee());
    }

    @Test
    void assignTo_erstzuweisung_schreibtAuditMitLeeremVorherUndGesetztemNachher() {
        Case assigned = createSampleCase().assignTo(Assignee.of("bruno.cm"), "tom.tl", NOW);

        AuditEntry entry = assigned.auditTrail().getLast();
        assertEquals(AuditAction.CASE_ASSIGNED, entry.action());
        assertEquals("tom.tl", entry.actor());
        assertNull(entry.previousAssignee());
        assertEquals(Assignee.of("bruno.cm"), entry.newAssignee());
    }

    @Test
    void assignTo_umverteilung_haeltBisherigeUndNeueZustaendigkeitFest() {
        Case assigned = createSampleCase().assignTo(Assignee.of("bruno.cm"), "tom.tl", NOW);

        Case reassigned = assigned.assignTo(Assignee.of("clara.cm"), "tom.tl", NOW);

        AuditEntry entry = reassigned.auditTrail().getLast();
        assertEquals(Assignee.of("bruno.cm"), entry.previousAssignee());
        assertEquals(Assignee.of("clara.cm"), entry.newAssignee());
    }

    @Test
    void assignTo_ohneZustaendigkeit_wirdAbgewiesen() {
        Case newCase = createSampleCase();

        assertThrows(NullPointerException.class, () -> newCase.assignTo(null, "tom.tl", NOW));
    }

    private static Case createSampleCase() {
        return Case.createNew(
                CaseNumber.of("CASE-000001"),
                CaseReference.of("KND-4711"),
                CaseType.LEISTUNG,
                Priority.HOCH,
                Source.TELEFON,
                "anna.cm",
                NOW);
    }
}
