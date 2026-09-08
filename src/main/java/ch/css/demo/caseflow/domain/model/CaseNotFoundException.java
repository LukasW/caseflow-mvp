package ch.css.demo.caseflow.domain.model;

/**
 * Signalisiert, dass ein Fall unter der angefragten Identität nicht existiert.
 * Domänen-Fehler — die Übersetzung in eine HTTP-Antwort (404) übernimmt der
 * REST-Adapter.
 */
public class CaseNotFoundException extends RuntimeException {

    public CaseNotFoundException(CaseId id) {
        super("Kein Fall mit der ID " + id.value());
    }
}
