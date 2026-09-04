package ch.css.demo.caseflow.adapter.out.persistence;

import ch.css.demo.caseflow.domain.model.Assignee;
import ch.css.demo.caseflow.domain.model.AuditEntry;
import ch.css.demo.caseflow.domain.model.Case;
import ch.css.demo.caseflow.domain.model.CaseId;
import ch.css.demo.caseflow.domain.model.CaseNumber;
import ch.css.demo.caseflow.domain.model.CaseReference;
import ch.css.demo.caseflow.domain.port.out.CaseRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Driven Adapter: implementiert {@link CaseRepository} auf Basis von Panache
 * und übersetzt zwischen Domänen-Aggregat und JPA-Entities.
 */
@ApplicationScoped
public class CaseRepositoryAdapter implements CaseRepository, PanacheRepositoryBase<CaseEntity, UUID> {

    @Override
    public CaseNumber nextCaseNumber() {
        Number sequence = (Number) getEntityManager()
                .createNativeQuery("SELECT nextval('case_number_seq')")
                .getSingleResult();
        return CaseNumber.of("CASE-%06d".formatted(sequence.longValue()));
    }

    @Override
    public Optional<Case> findById(CaseId id) {
        return findByIdOptional(id.value()).map(CaseRepositoryAdapter::toDomain);
    }

    @Override
    public Case save(Case aCase) {
        CaseEntity existing = findById(aCase.id().value());
        if (existing == null) {
            CaseEntity entity = toEntity(aCase);
            persist(entity);
            return toDomain(entity);
        }
        return toDomain(update(existing, aCase));
    }

    private static CaseEntity update(CaseEntity entity, Case aCase) {
        entity.status = aCase.status();
        entity.assignee = aCase.assignee() == null ? null : aCase.assignee().value();
        List<AuditEntry> trail = aCase.auditTrail();
        for (int i = entity.auditEntries.size(); i < trail.size(); i++) {
            entity.auditEntries.add(toAuditEntity(trail.get(i), entity));
        }
        return entity;
    }

    private static CaseEntity toEntity(Case aCase) {
        CaseEntity entity = new CaseEntity();
        entity.id = aCase.id().value();
        entity.caseNumber = aCase.number().value();
        entity.caseReference = aCase.reference().value();
        entity.caseType = aCase.type();
        entity.priority = aCase.priority();
        entity.source = aCase.source();
        entity.status = aCase.status();
        entity.assignee = aCase.assignee() == null ? null : aCase.assignee().value();
        entity.createdBy = aCase.createdBy();
        entity.createdAt = aCase.createdAt();
        aCase.auditTrail().forEach(auditEntry ->
                entity.auditEntries.add(toAuditEntity(auditEntry, entity)));
        return entity;
    }

    private static CaseAuditEntryEntity toAuditEntity(AuditEntry auditEntry, CaseEntity parent) {
        CaseAuditEntryEntity entity = new CaseAuditEntryEntity();
        entity.id = UUID.randomUUID();
        entity.caseEntity = parent;
        entity.action = auditEntry.action();
        entity.actor = auditEntry.actor();
        entity.previousAssignee = auditEntry.previousAssignee() == null
                ? null : auditEntry.previousAssignee().value();
        entity.newAssignee = auditEntry.newAssignee() == null
                ? null : auditEntry.newAssignee().value();
        entity.occurredAt = auditEntry.timestamp();
        return entity;
    }

    private static Case toDomain(CaseEntity entity) {
        List<AuditEntry> auditTrail = entity.auditEntries.stream()
                .map(audit -> new AuditEntry(
                        audit.action,
                        audit.actor,
                        audit.occurredAt,
                        toAssignee(audit.previousAssignee),
                        toAssignee(audit.newAssignee)))
                .toList();
        return new Case(
                CaseId.of(entity.id),
                CaseNumber.of(entity.caseNumber),
                CaseReference.of(entity.caseReference),
                entity.caseType,
                entity.priority,
                entity.source,
                entity.status,
                toAssignee(entity.assignee),
                entity.createdBy,
                entity.createdAt,
                auditTrail);
    }

    private static Assignee toAssignee(String value) {
        return value == null ? null : Assignee.of(value);
    }
}
