package ch.css.demo.caseflow.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Unveränderbarer Audit-Eintrag: hält fest, wer wann welche Statusänderung am
 * Fall ausgelöst hat. Teil des Aggregats, kein Logging-Nebeneffekt.
 *
 * <p>{@code previousAssignee} und {@code newAssignee} halten bei einer
 * Zuweisung die Zuständigkeit vor und nach der Änderung fest; für andere
 * Aktionen bleiben sie leer.
 */
public record AuditEntry(
        AuditAction action,
        String actor,
        Instant timestamp,
        Assignee previousAssignee,
        Assignee newAssignee) {

    public AuditEntry {
        Objects.requireNonNull(action, "action darf nicht null sein");
        Objects.requireNonNull(timestamp, "timestamp darf nicht null sein");
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("actor darf nicht leer sein");
        }
    }

    public static AuditEntry of(AuditAction action, String actor, Instant timestamp) {
        return new AuditEntry(action, actor, timestamp, null, null);
    }

    /** Audit-Eintrag einer Zuweisung mit bisheriger und neuer Zuständigkeit. */
    public static AuditEntry assigned(String actor, Instant timestamp,
            Assignee previousAssignee, Assignee newAssignee) {
        return new AuditEntry(AuditAction.CASE_ASSIGNED, actor, timestamp, previousAssignee, newAssignee);
    }
}
