package ch.css.demo.caseflow.application.service;

import ch.css.demo.caseflow.domain.model.Case;
import ch.css.demo.caseflow.domain.model.CaseNotFoundException;
import ch.css.demo.caseflow.domain.port.in.AssignCase;
import ch.css.demo.caseflow.domain.port.in.AssignCaseCommand;
import ch.css.demo.caseflow.domain.port.out.CaseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;

/**
 * Orchestriert den Anwendungsfall „Fall zuweisen": lädt das Aggregat, delegiert
 * die Zuweisung an die Domäne und persistiert das Ergebnis. Die Zuweisungsregel
 * und der Audit-Eintrag liegen im Aggregat, nicht hier.
 */
@ApplicationScoped
public class AssignCaseService implements AssignCase {

    private final CaseRepository caseRepository;
    private final Clock clock;

    public AssignCaseService(CaseRepository caseRepository, Clock clock) {
        this.caseRepository = caseRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Case handle(AssignCaseCommand command) {
        Case aCase = caseRepository.findById(command.caseId())
                .orElseThrow(() -> new CaseNotFoundException(command.caseId()));
        Case assigned = aCase.assignTo(command.assignee(), command.actor(), clock.instant());
        return caseRepository.save(assigned);
    }
}
