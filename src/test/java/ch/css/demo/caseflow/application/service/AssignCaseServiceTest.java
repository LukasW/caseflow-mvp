package ch.css.demo.caseflow.application.service;

import ch.css.demo.caseflow.domain.model.Assignee;
import ch.css.demo.caseflow.domain.model.AuditAction;
import ch.css.demo.caseflow.domain.model.Case;
import ch.css.demo.caseflow.domain.model.CaseId;
import ch.css.demo.caseflow.domain.model.CaseNotFoundException;
import ch.css.demo.caseflow.domain.model.CaseNumber;
import ch.css.demo.caseflow.domain.model.CaseReference;
import ch.css.demo.caseflow.domain.model.CaseType;
import ch.css.demo.caseflow.domain.model.Priority;
import ch.css.demo.caseflow.domain.model.Source;
import ch.css.demo.caseflow.domain.port.in.AssignCaseCommand;
import ch.css.demo.caseflow.domain.port.out.CaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Prüft die Orchestrierung von {@link AssignCaseService}: laden, zuweisen,
 * persistieren — und die Abweisung bei unbekanntem Fall.
 */
@ExtendWith(MockitoExtension.class)
class AssignCaseServiceTest {

    private static final Instant FIXED = Instant.parse("2026-09-04T10:15:30Z");

    @Mock
    private CaseRepository caseRepository;

    @Test
    void handle_weistFallZuUndPersistiert() {
        AssignCaseService service = new AssignCaseService(caseRepository, Clock.fixed(FIXED, ZoneOffset.UTC));
        Case existing = sampleCase();
        when(caseRepository.findById(existing.id())).thenReturn(Optional.of(existing));
        when(caseRepository.save(any(Case.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Case result = service.handle(new AssignCaseCommand(existing.id(), Assignee.of("bruno.cm"), "tom.tl"));

        assertEquals(Assignee.of("bruno.cm"), result.assignee());
        assertAssignmentAudited();
    }

    @Test
    void handle_unbekannterFall_wirdAbgewiesen() {
        AssignCaseService service = new AssignCaseService(caseRepository, Clock.fixed(FIXED, ZoneOffset.UTC));
        CaseId unknown = CaseId.generate();
        when(caseRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(CaseNotFoundException.class,
                () -> service.handle(new AssignCaseCommand(unknown, Assignee.of("bruno.cm"), "tom.tl")));
        verify(caseRepository, never()).save(any(Case.class));
    }

    private void assertAssignmentAudited() {
        ArgumentCaptor<Case> saved = ArgumentCaptor.forClass(Case.class);
        verify(caseRepository).save(saved.capture());
        assertEquals(AuditAction.CASE_ASSIGNED, saved.getValue().auditTrail().getLast().action());
        assertEquals(FIXED, saved.getValue().auditTrail().getLast().timestamp());
    }

    private static Case sampleCase() {
        return Case.createNew(
                CaseNumber.of("CASE-000001"),
                CaseReference.of("KND-4711"),
                CaseType.LEISTUNG,
                Priority.HOCH,
                Source.TELEFON,
                "anna.cm",
                FIXED);
    }
}
