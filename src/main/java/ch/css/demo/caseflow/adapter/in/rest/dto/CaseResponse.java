package ch.css.demo.caseflow.adapter.in.rest.dto;

import ch.css.demo.caseflow.domain.model.Case;
import ch.css.demo.caseflow.domain.model.CaseStatus;
import ch.css.demo.caseflow.domain.model.CaseType;
import ch.css.demo.caseflow.domain.model.Priority;
import ch.css.demo.caseflow.domain.model.Source;

import java.time.Instant;

/**
 * Antwortdarstellung eines erfassten Falls für das Frontend.
 */
public record CaseResponse(
        String id,
        String caseNumber,
        String caseReference,
        CaseType caseType,
        Priority priority,
        Source source,
        CaseStatus status,
        Instant createdAt) {

    public static CaseResponse from(Case aCase) {
        return new CaseResponse(
                aCase.id().value().toString(),
                aCase.number().value(),
                aCase.reference().value(),
                aCase.type(),
                aCase.priority(),
                aCase.source(),
                aCase.status(),
                aCase.createdAt());
    }
}
