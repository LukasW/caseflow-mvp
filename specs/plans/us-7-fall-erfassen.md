# US-1 — Fall erfassen (Issue #7)

## Übersicht

Erste vertikale Schicht des `Case`-Aggregats: ein `CASE_MANAGER` legt über
`POST /api/v1/cases` einen neuen Fall mit Grunddaten (Falltyp, Priorität, Quelle,
Kundenreferenz) an. Der Fall erhält eine eindeutige Fallnummer und startet im
Status `NEU`. Das Anlegen erzeugt einen unveränderbaren Audit-Eintrag
`CASE_CREATED`. Pflichtfelder werden serverseitig validiert (fail fast an der
REST-Grenze); nur `CASE_MANAGER` darf erfassen.

Greenfield: `domain/`, `application/service/` und `adapter/out/persistence/`
enthalten bisher nur `package-info.java`. Dieses Issue etabliert die
Aggregat-, Port-, Service-, Persistenz- und Migrationsstruktur, an der sich alle
folgenden Stories orientieren.

## Architektur-Entscheide

- **Hexagonal + DDD** streng nach `.github/copilot-instructions.md`. Domain ohne
  Framework-Imports (ArchUnit R1 erzwingt das).
- **Aggregate Root `Case`** als immutable Java `record` in `domain/model/`.
  Statusänderung liefert eine neue Instanz.
- **Value Objects**: `CaseType`, `Priority`, `Source`, `CaseStatus` als Java
  `enum` (fachlich geschlossene Wertelisten). `CaseReference` als `record`-VO mit
  Pflichtvalidierung im Compact Constructor. `CaseId` (UUID-Wrapper) und
  `CaseNumber` (fachliche Fallnummer) als `record`-VOs.
- **Audit-Trail als Domäne**: `AuditEntry` (`record`) + `AuditAction`-`enum` mit
  `CASE_CREATED`. Der Trail ist Teil des Aggregats (`List<AuditEntry>`), kein
  Logging-Nebeneffekt.
- **Fallnummer** aus einer Postgres-Sequence (`case_number_seq`), formatiert als
  `CASE-000001`. Der Repository-Port liefert die nächste Nummer
  (`nextCaseNumber()`), damit die Eindeutigkeit an der Persistenzgrenze entsteht.
- **Akteur & Zeitpunkt**: Der Application Service setzt `createdBy` (aus der
  `SecurityIdentity`, via Command übergeben) und `createdAt` (injizierte `Clock`
  für Testbarkeit). Keine Geschäftsregeln im Service — nur Orchestrierung.
- **Persistenz**: JPA-`CaseEntity` + `CaseAuditEntryEntity` (public Fields erlaubt)
  in `adapter/out/persistence/`; Mapping Domain ↔ Entity im Adapter. Panache
  `PanacheRepositoryBase<CaseEntity, UUID>`.
- **REST**: `@RolesAllowed(Roles.CASE_MANAGER)`, Bean Validation (`@NotNull`) auf
  dem Request-DTO für fail-fast-Pflichtfeldprüfung. DTOs als `record` mit
  `@JsonProperty` ausschließlich in `adapter/in/rest/dto/` (ArchUnit R4).

## Zu erstellende Dateien

### Domain (`src/main/java/ch/css/demo/caseflow/domain/`)

- [ ] `model/Case.java` — Aggregate Root (`record`); Factory-Methode
      `createNew(...)` setzt Status `NEU` und ersten `AuditEntry CASE_CREATED`;
      Zugriff auf Audit-Trail als unveränderliche Liste.
- [ ] `model/CaseId.java` — VO (`record` um `UUID`), `generate()`-Factory.
- [ ] `model/CaseNumber.java` — VO (`record` um `String`), Formatvalidierung.
- [ ] `model/CaseReference.java` — VO (`record`), nicht leer (Compact Constructor).
- [ ] `model/CaseType.java` — `enum` (fachliche Falltypen, z. B. `LEISTUNG`,
      `BESCHWERDE`, `ANFRAGE` — Auswahl im Plan-Review bestätigen).
- [ ] `model/Priority.java` — `enum` (`NIEDRIG`, `MITTEL`, `HOCH`).
- [ ] `model/Source.java` — `enum` (`TELEFON`, `E_MAIL`, `BRIEF`, `PORTAL`).
- [ ] `model/CaseStatus.java` — `enum` (`NEU`, `IN_BEARBEITUNG`, `ABGESCHLOSSEN`,
      … — für US-1 nur `NEU` gesetzt, Rest für Folgestories vorbereitet).
- [ ] `model/AuditEntry.java` — VO (`record`): `AuditAction`, `actor`,
      `timestamp`, optionale `note`.
- [ ] `model/AuditAction.java` — `enum` mit `CASE_CREATED`.
- [ ] `port/in/CreateCase.java` — Use-Case-Interface `Case handle(CreateCaseCommand)`.
- [ ] `port/in/CreateCaseCommand.java` — `record` (Falltyp, Priorität, Quelle,
      Kundenreferenz, Akteur).
- [ ] `port/out/CaseRepository.java` — `save(Case)`, `nextCaseNumber()`.

### Application (`src/main/java/ch/css/demo/caseflow/application/service/`)

- [ ] `CreateCaseService.java` — `@ApplicationScoped`, implementiert `CreateCase`;
      `@Transactional`; Constructor Injection von `CaseRepository` + `Clock`;
      erzeugt `Case` über die Domain-Factory und persistiert.

### Adapter out — Persistence (`.../adapter/out/persistence/`)

- [ ] `CaseEntity.java` — JPA-Entity (`case`-Tabelle).
- [ ] `CaseAuditEntryEntity.java` — JPA-Entity (`case_audit_entry`-Tabelle,
      FK auf `case`).
- [ ] `CaseRepositoryAdapter.java` — `@ApplicationScoped`, implementiert
      `CaseRepository`, `PanacheRepositoryBase<CaseEntity, UUID>`; Mapping
      Domain ↔ Entity; `nextCaseNumber()` via Sequence.

### Adapter in — REST (`.../adapter/in/rest/`)

- [ ] `CaseResource.java` — `@Path("/api/v1/cases")`, `POST`,
      `@RolesAllowed(Roles.CASE_MANAGER)`; Constructor Injection von `CreateCase`
      + `SecurityIdentity`; gibt `201 Created` mit `Location` + `CaseResponse`.
- [ ] `dto/CreateCaseRequest.java` — `record` mit `@NotNull`-Feldern
      (`case_type`, `priority`, `source`, `case_reference`).
- [ ] `dto/CaseResponse.java` — `record` (Fallnummer, Status, Grunddaten,
      `created_at`).

### Migration (`src/main/resources/db/`)

- [ ] `migration/0002-create-case.xml` — Tabellen `case` + `case_audit_entry`,
      Sequence `case_number_seq`.
- [ ] `changeLog.xml` — Include für `0002-create-case.xml` ergänzen.

### Tests

- [ ] `domain/model/CaseTest.java` — Invarianten: neuer Fall Status `NEU`,
      erster Audit-Eintrag `CASE_CREATED`, VO-Validierung (leere Referenz wirft).
- [ ] `application/service/CreateCaseServiceTest.java` — Unit mit gemocktem
      `CaseRepository` + fixe `Clock`; prüft Fallnummernvergabe, Actor/Zeitpunkt,
      `save`-Aufruf.
- [ ] `adapter/in/rest/CaseResourceIT.java` — `@QuarkusTest` + REST-assured +
      `@TestSecurity`: Happy Path (201, Status `NEU`), fehlendes Pflichtfeld (400
      inkl. Feldname), Rolle `AUDITOR` (403).
- [ ] `src/test/resources/features/case/fall-erfassen.feature` — Titel/Tag `US-1`,
      **exakt 3 Szenarien** (SpecScenarioParityTest).
- [ ] `cucumber/steps/CaseSteps.java` — Step-Definitionen (POST mit Body) auf Basis
      von `CommonSteps.authenticated()`.

## Zu ändernde Dateien

- [ ] `specs/user-stories/1-fall-erfassen.md` — Aufgaben-Checkliste abhaken,
      Plan-Backlink in Kopf-Tabelle.

## Test-Strategie

- **Unit (Domain)**: Aggregat-Invarianten und VO-Validierung ohne Framework.
- **Unit (Application)**: `CreateCaseService` mit gemocktem Port + fixer `Clock`.
- **Integration (`*IT`, `@QuarkusTest`)**: REST-Contract inkl. Auth (201/400/403)
  gegen Dev-Services-PostgreSQL.
- **BDD (Backend, Java-Cucumber-Runner, ohne Tag)**: 3 Szenarien aus der Story
  1:1 als Gherkin; Parität via `SpecScenarioParityTest`.
- **Guardrails**: `HexagonArchitectureTest` (Schichtdisziplin), JaCoCo (>90 % auf
  `domain/service/*` — hier nicht betroffen, da keine Domain Services).

## Offene Fragen

- **Falltyp-/Quelle-Werte**: konkrete Enum-Ausprägungen (`CaseType`, `Source`)
  sind fachlich noch nicht fixiert — Vorschlag oben; im Review bestätigen oder
  anpassen.
- **Fallnummern-Format**: `CASE-000001` (Sequence-basiert) vorgeschlagen —
  alternativ Jahr-Präfix (`2026-000001`)?
