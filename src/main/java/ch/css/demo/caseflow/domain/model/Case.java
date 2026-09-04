package ch.css.demo.caseflow.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root des Fachbereichs Fallbearbeitung. Konsistenz- und
 * Transaktionsgrenze: einziger Einstiegspunkt ins Aggregat. Immutable — jede
 * Zustandsänderung liefert eine neue Instanz, jede Statusänderung erzeugt einen
 * Audit-Eintrag.
 */
public record Case(
        CaseId id,
        CaseNumber number,
        CaseReference reference,
        CaseType type,
        Priority priority,
        Source source,
        CaseStatus status,
        String createdBy,
        Instant createdAt,
        List<AuditEntry> auditTrail) {

    public Case {
        Objects.requireNonNull(id, "id darf nicht null sein");
        Objects.requireNonNull(number, "number darf nicht null sein");
        Objects.requireNonNull(reference, "reference darf nicht null sein");
        Objects.requireNonNull(type, "type darf nicht null sein");
        Objects.requireNonNull(priority, "priority darf nicht null sein");
        Objects.requireNonNull(source, "source darf nicht null sein");
        Objects.requireNonNull(status, "status darf nicht null sein");
        Objects.requireNonNull(createdAt, "createdAt darf nicht null sein");
        Objects.requireNonNull(auditTrail, "auditTrail darf nicht null sein");
        if (createdBy == null || createdBy.isBlank()) {
            throw new IllegalArgumentException("createdBy darf nicht leer sein");
        }
        auditTrail = List.copyOf(auditTrail);
    }

    /**
     * Erfasst einen neuen Fall: Status {@link CaseStatus#NEU}, erster
     * Audit-Eintrag {@link AuditAction#CASE_CREATED}.
     */
    public static Case createNew(CaseNumber number, CaseReference reference, CaseType type,
            Priority priority, Source source, String actor, Instant timestamp) {
        AuditEntry created = AuditEntry.of(AuditAction.CASE_CREATED, actor, timestamp);
        return new Case(CaseId.generate(), number, reference, type, priority, source,
                CaseStatus.NEU, actor, timestamp, List.of(created));
    }
}
