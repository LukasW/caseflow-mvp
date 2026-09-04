package ch.css.demo.caseflow.application.service;

import ch.css.demo.caseflow.domain.model.Case;
import ch.css.demo.caseflow.domain.model.CaseNumber;
import ch.css.demo.caseflow.domain.port.in.CreateCase;
import ch.css.demo.caseflow.domain.port.in.CreateCaseCommand;
import ch.css.demo.caseflow.domain.port.out.CaseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Clock;

/**
 * Orchestriert den Anwendungsfall „Fall erfassen": vergibt die Fallnummer,
 * erzeugt das Aggregat über die Domain-Factory und persistiert es. Die
 * Geschäftsregeln (Startstatus, Audit-Eintrag) liegen im Aggregat, nicht hier.
 */
@ApplicationScoped
public class CreateCaseService implements CreateCase {

    private final CaseRepository caseRepository;
    private final Clock clock;

    public CreateCaseService(CaseRepository caseRepository, Clock clock) {
        this.caseRepository = caseRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Case handle(CreateCaseCommand command) {
        CaseNumber number = caseRepository.nextCaseNumber();
        Case newCase = Case.createNew(number, command.reference(), command.type(),
                command.priority(), command.source(), command.actor(), clock.instant());
        return caseRepository.save(newCase);
    }
}
