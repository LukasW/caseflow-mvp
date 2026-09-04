package ch.css.demo.caseflow.domain.port.in;

import ch.css.demo.caseflow.domain.model.Case;

/**
 * Driving Port: Anwendungsfall „Fall zuweisen und umverteilen". Von den Driving
 * Adaptern aufgerufen, in der Application-Schicht implementiert.
 */
public interface AssignCase {

    /**
     * Weist den referenzierten Fall der übergebenen Person zu bzw. verteilt ihn
     * um und gibt das persistierte Aggregat mit Audit-Eintrag zurück.
     */
    Case handle(AssignCaseCommand command);
}
