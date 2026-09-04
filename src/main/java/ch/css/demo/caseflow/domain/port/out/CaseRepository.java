package ch.css.demo.caseflow.domain.port.out;

import ch.css.demo.caseflow.domain.model.Case;
import ch.css.demo.caseflow.domain.model.CaseNumber;

/**
 * Driven Port: Collection-Abstraktion für das {@link Case}-Aggregat.
 * Nimmt und liefert Domänenmodelle, nie Persistenz-Entities.
 */
public interface CaseRepository {

    /** Vergibt die nächste eindeutige Fallnummer. */
    CaseNumber nextCaseNumber();

    /** Persistiert das Aggregat und gibt den gespeicherten Stand zurück. */
    Case save(Case aCase);
}
