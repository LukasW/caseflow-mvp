package ch.css.demo.caseflow.adapter.out.persistence;

import ch.css.demo.caseflow.domain.model.CaseStatus;
import ch.css.demo.caseflow.domain.model.CaseType;
import ch.css.demo.caseflow.domain.model.Priority;
import ch.css.demo.caseflow.domain.model.Source;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA-Abbild des {@link ch.css.demo.caseflow.domain.model.Case}-Aggregats.
 * Reines Persistenz-Artefakt in der Adapter-Schicht — nicht mit dem
 * Domänen-Aggregat verwechseln.
 */
@Entity
@Table(name = "cases")
public class CaseEntity {

    @Id
    public UUID id;

    @Column(name = "case_number", nullable = false, unique = true)
    public String caseNumber;

    @Column(name = "case_reference", nullable = false)
    public String caseReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_type", nullable = false)
    public CaseType caseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    public Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    public Source source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public CaseStatus status;

    @Column(name = "created_by", nullable = false)
    public String createdBy;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<CaseAuditEntryEntity> auditEntries = new ArrayList<>();
}
