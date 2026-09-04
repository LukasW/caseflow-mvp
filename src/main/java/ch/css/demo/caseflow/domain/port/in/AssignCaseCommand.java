package ch.css.demo.caseflow.domain.port.in;

import ch.css.demo.caseflow.domain.model.Assignee;
import ch.css.demo.caseflow.domain.model.CaseId;

/**
 * Eingabedaten für das Zuweisen eines Falls: welcher Fall, an wen und durch wen.
 */
public record AssignCaseCommand(CaseId caseId, Assignee assignee, String actor) {
}
