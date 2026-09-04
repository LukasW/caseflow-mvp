package ch.css.demo.caseflow.application.service;

import ch.css.demo.caseflow.domain.model.AuditAction;
import ch.css.demo.caseflow.domain.model.Case;
import ch.css.demo.caseflow.domain.model.CaseNumber;
import ch.css.demo.caseflow.domain.model.CaseReference;
import ch.css.demo.caseflow.domain.model.CaseStatus;
import ch.css.demo.caseflow.domain.model.CaseType;
import ch.css.demo.caseflow.domain.model.Priority;
import ch.css.demo.caseflow.domain.model.Source;
import ch.css.demo.caseflow.domain.port.in.CreateCaseCommand;
import ch.css.demo.caseflow.domain.port.out.CaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Prüft die Orchestrierung von {@link CreateCaseService} mit gemocktem Port und
 * fixer Uhr: Fallnummernvergabe, Akteur und Erfassungszeitpunkt.
 */
@ExtendWith(MockitoExtension.class)
class CreateCaseServiceTest {

    private static final Instant FIXED = Instant.parse("2026-09-04T10:15:30Z");

    @Mock
    private CaseRepository caseRepository;

    @Test
    void handle_vergibtFallnummerUndErfasstImStatusNeu() {
        CreateCaseService service = new CreateCaseService(caseRepository, Clock.fixed(FIXED, ZoneOffset.UTC));
        when(caseRepository.nextCaseNumber()).thenReturn(CaseNumber.of("CASE-000001"));
        when(caseRepository.save(any(Case.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Case result = service.handle(new CreateCaseCommand(
                CaseType.BESCHWERDE, Priority.MITTEL, Source.E_MAIL,
                CaseReference.of("KND-4711"), "anna.cm"));

        assertEquals(CaseNumber.of("CASE-000001"), result.number());
        assertEquals(CaseStatus.NEU, result.status());
        assertEquals("anna.cm", result.createdBy());
        assertEquals(FIXED, result.createdAt());
        assertEquals(AuditAction.CASE_CREATED, result.auditTrail().getFirst().action());
    }

    @Test
    void handle_persistiertDasAggregat() {
        CreateCaseService service = new CreateCaseService(caseRepository, Clock.fixed(FIXED, ZoneOffset.UTC));
        when(caseRepository.nextCaseNumber()).thenReturn(CaseNumber.of("CASE-000002"));
        when(caseRepository.save(any(Case.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.handle(new CreateCaseCommand(
                CaseType.ANFRAGE, Priority.NIEDRIG, Source.PORTAL,
                CaseReference.of("KND-0001"), "anna.cm"));

        ArgumentCaptor<Case> saved = ArgumentCaptor.forClass(Case.class);
        org.mockito.Mockito.verify(caseRepository).save(saved.capture());
        assertEquals(CaseType.ANFRAGE, saved.getValue().type());
    }
}
