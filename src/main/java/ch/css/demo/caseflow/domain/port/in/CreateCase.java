package ch.css.demo.caseflow.domain.port.in;

import ch.css.demo.caseflow.domain.model.Case;

/**
 * Driving Port: Anwendungsfall „Fall erfassen". Von den Driving Adaptern
 * aufgerufen, in der Application-Schicht implementiert.
 */
public interface CreateCase {

    /**
     * Erfasst einen neuen Fall aus den übergebenen Grunddaten und gibt das
     * persistierte Aggregat mit Fallnummer und Audit-Eintrag zurück.
     */
    Case handle(CreateCaseCommand command);
}
