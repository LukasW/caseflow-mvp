package ch.css.demo.caseflow.domain.port.in;

import ch.css.demo.caseflow.domain.model.CaseReference;
import ch.css.demo.caseflow.domain.model.CaseType;
import ch.css.demo.caseflow.domain.model.Priority;
import ch.css.demo.caseflow.domain.model.Source;

import java.util.Objects;

/**
 * Eingabedaten für das Erfassen eines Falls. Der Akteur wird vom Driving Adapter
 * aus der Sicherheitsidentität übergeben.
 */
public record CreateCaseCommand(
        CaseType type,
        Priority priority,
        Source source,
        CaseReference reference,
        String actor) {

    public CreateCaseCommand {
        Objects.requireNonNull(type, "type darf nicht null sein");
        Objects.requireNonNull(priority, "priority darf nicht null sein");
        Objects.requireNonNull(source, "source darf nicht null sein");
        Objects.requireNonNull(reference, "reference darf nicht null sein");
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("actor darf nicht leer sein");
        }
    }
}
