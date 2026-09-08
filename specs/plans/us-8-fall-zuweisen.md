# US-2 (Issue #8) — Fall zuweisen und umverteilen

## Übersicht

Backend-Umsetzung der Zuweisung und Umverteilung eines Falls durch einen
`TEAM_LEAD`. Das bestehende `Case`-Aggregat wird um eine Zuständigkeit
(`Assignee`, IAM-Referenz-ID) erweitert. Eine neue Domänenmethode `assignTo`
setzt die Zuständigkeit und schreibt einen Audit-Eintrag `CASE_ASSIGNED` mit
Vorher- und Nachher-Zuständigkeit. Ein neuer Use-Case `AssignCase` orchestriert
Laden → `assignTo` → Persistieren; ein REST-Endpunkt
`PUT /api/v1/cases/{id}/assignment` (nur `TEAM_LEAD`) bildet die Aktion ab.

**Scope:** Backend (Domain, Application, Adapter in/out, Migration, Tests, BDD).
Die Frontend-Aktion („Zuweisen"/„Umverteilen") lebt laut Story in der
Fall-Detailansicht aus **US-8 (#14)** und ist hier **out of scope** — die
Aufgaben- und Reviewer-Liste der Story ist backend-only (`hexagonal-reviewer`,
`auth-security-reviewer`, keine Angular-Reviewer).

## Architektur-Entscheide

- **Domain** setzt die Invariante durch: `Case.assignTo` erzeugt eine neue
  Aggregat-Instanz und hängt den Audit-Eintrag an — keine Geschäftsregel im
  Application Service.
- **Zuständigkeit** als Value Object `Assignee` (reine IAM-Referenz-ID,
  non-blank). Kein eigenes `User`-Aggregat (siehe Story „Technische Notizen").
- **Audit Vorher/Nachher**: `AuditEntry` erhält zwei nullbare Felder
  `previousAssignee` / `newAssignee`. Für `CASE_CREATED` bleiben sie leer.
- **Umverteilung = dieselbe Aktion** wie Erstzuweisung (kein Sonderpfad); der
  Unterschied liegt nur im gesetzten `previousAssignee`.
- **Persistenz-Update**: `CaseRepository.save` wird zum Upsert — bei bekanntem
  Aggregat werden Skalarfelder aktualisiert und nur die **neuen** Audit-Einträge
  angehängt (bestehende Zeilen bleiben unangetastet).
- **Rollenschutz** serverseitig via `@RolesAllowed(Roles.TEAM_LEAD)`.

## Zu erstellende Dateien

- [ ] `src/main/java/ch/css/demo/caseflow/domain/model/Assignee.java` — Value Object (IAM-Referenz-ID, non-blank, trim)
- [ ] `src/main/java/ch/css/demo/caseflow/domain/model/CaseNotFoundException.java` — Domänen-Exception bei unbekanntem Fall
- [ ] `src/main/java/ch/css/demo/caseflow/domain/port/in/AssignCase.java` — Driving Port
- [ ] `src/main/java/ch/css/demo/caseflow/domain/port/in/AssignCaseCommand.java` — Command (`CaseId`, `Assignee`, `actor`)
- [ ] `src/main/java/ch/css/demo/caseflow/application/service/AssignCaseService.java` — Orchestrierung, `@Transactional`
- [ ] `src/main/java/ch/css/demo/caseflow/adapter/in/rest/dto/AssignCaseRequest.java` — Body `{ assignee }`, `@NotBlank`
- [ ] `src/main/java/ch/css/demo/caseflow/adapter/in/rest/CaseNotFoundExceptionMapper.java` — `CaseNotFoundException` → 404
- [ ] `src/main/resources/db/migration/0003-case-assignment.xml` — Spalten `assignee`, `previous_assignee`, `new_assignee`
- [ ] `src/test/resources/features/case/fall-zuweisen.feature` — `US-2`, 3 Szenarien
- [ ] `src/test/java/ch/css/demo/caseflow/cucumber/steps/CaseAssignmentSteps.java` — fachliche Steps
- [ ] `src/test/java/ch/css/demo/caseflow/application/service/AssignCaseServiceTest.java` — Unit (Mock-Port)
- [ ] `src/test/java/ch/css/demo/caseflow/adapter/in/rest/CaseAssignmentIT.java` — `@QuarkusTest` (200 / 403 / 404)

## Zu ändernde Dateien

- [ ] `domain/model/Case.java` — nullbares Feld `assignee`, Methode `assignTo(Assignee, actor, timestamp)`, `createNew` setzt `assignee = null`
- [ ] `domain/model/AuditEntry.java` — Felder `previousAssignee`/`newAssignee` (nullbar), Factory `assigned(...)`, `of(...)` bleibt für `CASE_CREATED`
- [ ] `domain/model/AuditAction.java` — Konstante `CASE_ASSIGNED`
- [ ] `domain/port/out/CaseRepository.java` — `Optional<Case> findById(CaseId)`
- [ ] `adapter/out/persistence/CaseEntity.java` — Spalte `assignee` (nullbar)
- [ ] `adapter/out/persistence/CaseAuditEntryEntity.java` — Spalten `previous_assignee`/`new_assignee` (nullbar)
- [ ] `adapter/out/persistence/CaseRepositoryAdapter.java` — `findById`, `save` als Upsert, Mapping `assignee` + Vorher/Nachher
- [ ] `adapter/in/rest/CaseResource.java` — `PUT /{id}/assignment`, `AssignCase` injizieren, `@RolesAllowed(TEAM_LEAD)`
- [ ] `adapter/in/rest/dto/CaseResponse.java` — nullbares Feld `assignee`
- [ ] `src/main/resources/db/changeLog.xml` — Include `0003-case-assignment.xml`
- [ ] `src/test/java/ch/css/demo/caseflow/domain/model/CaseTest.java` — Tests für `assignTo` (Erstzuweisung, Umverteilung, Audit Vorher/Nachher, keine Unassign)
- [ ] `specs/user-stories/2-fall-zuweisen.md` — Plan-Backlink in Kopf-Tabelle

## Test-Strategie

- [ ] **Unit (Domain)** in `CaseTest`:
  - `assignTo` setzt neue Zuständigkeit und liefert neue Instanz (Immutability)
  - Erstzuweisung → Audit `CASE_ASSIGNED`, Vorher leer, Nachher gesetzt
  - Umverteilung → Audit hält bisherige und neue Zuständigkeit
  - `assignTo(null, …)` wird abgewiesen
- [ ] **Unit (Application)** `AssignCaseServiceTest` (Mock-`CaseRepository`, fixe `Clock`):
  - lädt Fall, ruft `assignTo`, persistiert; unbekannter Fall → `CaseNotFoundException`
- [ ] **Integration** `CaseAssignmentIT` (`@QuarkusTest`, Dev-Services-DB):
  - `TEAM_LEAD` weist zu → 200, Antwort trägt `assignee`
  - `CASE_MANAGER` → 403, Zuständigkeit unverändert
  - unbekannte `id` → 404
- [ ] **BDD** `fall-zuweisen.feature` (Java-Runner, ohne `@E2E`) — 3 Szenarien deckungsgleich zur Story:
  1. Fall erstmalig zuweisen (Happy Path) — Audit Vorher leer / Nachher gesetzt
  2. Zugewiesenen Fall umverteilen (Happy Path) — Audit Vorher + Nachher
  3. `CASE_MANAGER` darf nicht zuweisen (Berechtigung) — 403, Zuständigkeit unverändert
- [ ] **Guardrails** bleiben grün: `HexagonArchitectureTest`, `SpecScenarioParityTest` (3 = 3).

## Offene Fragen

- Keine. Ungültige `id`-Formate und Fremdzugriff auf fremde Fälle (Sichtbarkeit)
  gehören zu US-8 (#14) und sind hier bewusst nicht abgedeckt.
