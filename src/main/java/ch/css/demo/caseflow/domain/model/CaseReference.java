package ch.css.demo.caseflow.domain.model;

/**
 * Referenz auf das auslösende Kundenanliegen (Kundenreferenz als reine ID).
 * Wird nicht gegen das Kernsystem validiert, muss aber gesetzt sein.
 */
public record CaseReference(String value) {

    public CaseReference {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Kundenreferenz darf nicht leer sein");
        }
        value = value.trim();
    }

    public static CaseReference of(String value) {
        return new CaseReference(value);
    }
}
