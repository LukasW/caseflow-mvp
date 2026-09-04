package ch.css.demo.caseflow.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
