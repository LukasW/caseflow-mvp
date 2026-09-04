package ch.css.demo.caseflow.domain.model;

/**
 * Zuständige Person eines Falls als reine Referenz-ID aus dem IAM. Wird nicht
 * gegen das Identitätssystem validiert, muss aber gesetzt sein.
 */
public record Assignee(String value) {

    public Assignee {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Zuständigkeit darf nicht leer sein");
        }
        value = value.trim();
    }

    public static Assignee of(String value) {
        return new Assignee(value);
    }
}
