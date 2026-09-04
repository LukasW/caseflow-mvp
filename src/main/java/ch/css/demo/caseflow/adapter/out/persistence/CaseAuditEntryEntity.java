package ch.css.demo.caseflow.adapter.out.persistence;

import ch.css.demo.caseflow.domain.model.AuditAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA-Abbild eines Audit-Eintrags des Falls. Gehört zum {@link CaseEntity}
 * und wird ausschliesslich über dieses Aggregat geschrieben.
 */
@Entity
@Table(name = "case_audit_entry")
public class CaseAuditEntryEntity {

    @Id
    public UUID id;

    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    public CaseEntity caseEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    public AuditAction action;

    @Column(name = "actor", nullable = false)
    public String actor;

    @Column(name = "previous_assignee")
    public String previousAssignee;

    @Column(name = "new_assignee")
    public String newAssignee;

    @Column(name = "occurred_at", nullable = false)
    public Instant occurredAt;
}
