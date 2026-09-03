---
applyTo: "src/main/java/**/*.java"
---

# Backend — Hexagonal Architecture & DDD Tactical Design

Gilt für allen Java-Code unter `src/main/java/ch/css/demo/caseflow/`. Verstösse
brechen den Build via ArchUnit (`HexagonArchitectureTest`). Nach Änderungen den
Custom Agent `hexagonal-reviewer` laufen lassen.

## Hexagonal — wo Code lebt

1. **`domain/` ist framework-frei**: keine Imports aus `jakarta.*`, `io.quarkus.*`,
   `org.hibernate.*`, `com.fasterxml.jackson.*`; keine `@Entity`, `@ApplicationScoped`,
   `@Inject`, `@JsonProperty`, `@Path`. Nur Records, Interfaces, Enums, JDK.
2. **Ports**: Driving Ports (Use-Case-Interfaces) in `domain/port/in/`, Driven Ports
   (Repositories, externe Services) in `domain/port/out/`. Driving-Port-Implementierungen
   in `application/service/`, Driven-Port-Implementierungen in `adapter/out/**`.
3. **JPA-Entities nur in `adapter/out/persistence/`** — ebenso die Mapper zwischen
   Domänen-Records und JPA-Entities.
4. **REST-Resources ohne Business-Logik**: `@Path`-Klassen in `adapter/in/rest/`
   delegieren an Use-Case-Ports, greifen nie direkt auf Repositories zu. DTOs
   ausschliesslich in `adapter/in/rest/dto/`.
5. **Abhängigkeitsrichtung**: Adapter → Application → Domain. Domain importiert nichts
   aus `application/` oder `adapter/`; Application nichts aus `adapter/`.
6. **Namen**: Domänen-Records `Case`, `Deadline`, `AuditEntry` (kein `*Aggregate`/`*VO`);
   JPA `CaseEntity`; Services `CaseService` (implementiert `CreateCaseUseCase` o. ä.);
   Repository-Port `CaseRepository`, Impl `CaseRepositoryAdapter`.

## DDD — welche Form der Domänencode hat

7. **Aggregate Root ist der einzige Einstiegspunkt**: Aussenwelt lädt/referenziert nur
   Aggregate Roots; innere Entities nur über Methoden des Roots.
8. **Value Objects** sind `record`, immutable, ohne `id`; Invarianten im Compact
   Constructor (fail fast).
9. **Aggregat-übergreifende Referenzen per ID**, nie per Objektreferenz.
10. **Invarianten im Aggregat, nicht im Service**: Application Service = laden →
    Domänenmethode → speichern → Event publizieren. Keine fachlichen Conditionals dort.
11. **Immutability**: Zustandsänderung liefert eine neue Instanz (kein Setter, kein
    veränderbares Feld).
12. **Domain Events**: immutable Records in `domain/event/`, Vergangenheitsform
    (`CaseAssigned`), publiziert über einen Driven Port **nach** dem Persistieren.
13. **Ein Repository pro Aggregate Root**; nimmt/liefert Domänen-Records, nie JPA-Entities.
14. **Application Service vs. Domain Service**: `application/service/` orchestriert einen
    Use Case (Transaktionsgrenze); `domain/service/` hält pure, zustandslose Domänenlogik.
15. **Audit-Trail**: Jede Statusänderung am Aggregat erzeugt einen Audit-Eintrag — Teil
    der Domäne, kein Logging-Nebeneffekt.

**KISS**: `domain/event/`, `domain/service/`, `domain/factory/` erst anlegen, wenn ein
echter Use Case sie braucht.

## Java-Konventionen

- Records für Domänenmodelle, Events und DTOs; `@ApplicationScoped` für Services und
  Repositories; Constructor Injection.
- Panache: `implements PanacheRepositoryBase<T, ID>` als Adapter hinter dem Port.
- Kein `null`-Return — `Optional`. Keine Magic Numbers. Early Returns.
- Javadoc nur auf public API (Ports, Services, Resources), erklärt **warum**.
- Neue Aggregate bringen Liquibase-Migration in `db/migration/` + Include in
  `db/changeLog.xml` mit (Hibernate validiert nur).
- Tests: `*Test` = Unit (Driven Ports mocken), `*IT` = `@QuarkusTest`.
