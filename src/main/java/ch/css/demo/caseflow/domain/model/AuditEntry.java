package ch.css.demo.caseflow.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Unveränderbarer Audit-Eintrag: hält fest, wer wann welche Statusänderung am
 * Fall ausgelöst hat. Teil des Aggregats, kein Logging-Nebeneffekt.
 */
public record AuditEntry(AuditAction action, String actor, Instant timestamp) {

    public AuditEntry {
        Objects.requireNonNull(action, "action darf nicht null sein");
        Objects.requireNonNull(timestamp, "timestamp darf nicht null sein");
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("actor darf nicht leer sein");
        }
    }

    public static AuditEntry of(AuditAction action, String actor, Instant timestamp) {
        return new AuditEntry(action, actor, timestamp);
    }
}
