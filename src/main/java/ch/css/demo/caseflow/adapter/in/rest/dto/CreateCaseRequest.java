package ch.css.demo.caseflow.adapter.in.rest.dto;

import ch.css.demo.caseflow.domain.model.CaseType;
import ch.css.demo.caseflow.domain.model.Priority;
import ch.css.demo.caseflow.domain.model.Source;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Eingabedaten zum Erfassen eines Falls. Pflichtfelder werden an der
 * REST-Grenze validiert (fail fast); die Meldung nennt das fehlende Feld.
 */
public record CreateCaseRequest(
        @NotNull(message = "Falltyp ist erforderlich") CaseType caseType,
        @NotNull(message = "Priorität ist erforderlich") Priority priority,
        @NotNull(message = "Quelle ist erforderlich") Source source,
        @NotBlank(message = "Kundenreferenz ist erforderlich") String caseReference) {
}
