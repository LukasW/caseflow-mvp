package ch.css.demo.caseflow.adapter.out.persistence;

import ch.css.demo.caseflow.domain.model.AuditEntry;
import ch.css.demo.caseflow.domain.model.Case;
import ch.css.demo.caseflow.domain.model.CaseId;
import ch.css.demo.caseflow.domain.model.CaseNumber;
import ch.css.demo.caseflow.domain.model.CaseReference;
import ch.css.demo.caseflow.domain.port.out.CaseRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
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
    public Case save(Case aCase) {
        CaseEntity entity = toEntity(aCase);
        persist(entity);
        return toDomain(entity);
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
        entity.createdBy = aCase.createdBy();
        entity.createdAt = aCase.createdAt();
        entity.auditEntries = aCase.auditTrail().stream()
                .map(auditEntry -> toAuditEntity(auditEntry, entity))
                .toList();
        return entity;
    }

    private static CaseAuditEntryEntity toAuditEntity(AuditEntry auditEntry, CaseEntity parent) {
        CaseAuditEntryEntity entity = new CaseAuditEntryEntity();
        entity.id = UUID.randomUUID();
        entity.caseEntity = parent;
        entity.action = auditEntry.action();
        entity.actor = auditEntry.actor();
        entity.occurredAt = auditEntry.timestamp();
        return entity;
    }

    private static Case toDomain(CaseEntity entity) {
        List<AuditEntry> auditTrail = entity.auditEntries.stream()
                .map(audit -> AuditEntry.of(audit.action, audit.actor, audit.occurredAt))
                .toList();
        return new Case(
                CaseId.of(entity.id),
                CaseNumber.of(entity.caseNumber),
                CaseReference.of(entity.caseReference),
                entity.caseType,
                entity.priority,
                entity.source,
                entity.status,
                entity.createdBy,
                entity.createdAt,
                auditTrail);
    }
}
