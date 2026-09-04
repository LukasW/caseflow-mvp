package ch.css.demo.caseflow.domain.model;

import java.util.UUID;

/**
 * Identität eines {@link Case}-Aggregats. Wird beim Anlegen technisch erzeugt.
 */
public record CaseId(UUID value) {

    public CaseId {
        if (value == null) {
            throw new IllegalArgumentException("CaseId darf nicht null sein");
        }
    }

    public static CaseId generate() {
        return new CaseId(UUID.randomUUID());
    }

    public static CaseId of(UUID value) {
        return new CaseId(value);
    }
}
