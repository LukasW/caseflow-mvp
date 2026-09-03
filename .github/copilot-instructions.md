# CaseFlow — Copilot-Instruktionen

Repository-weite Anweisungen für GitHub Copilot (Chat, Coding Agent, CLI).
Ergänzt durch pfadbezogene Regeln in `.github/instructions/`, Custom Agents in
`.github/agents/`, Agent Skills in `.github/skills/`, Prompt-Dateien in
`.github/prompts/` und Hooks in `.github/hooks/`.

## Projekt

CaseFlow ist ein Case-Management-Tool für Case Manager der CSS: ein Quarkus
Backend-For-Frontend, das eine Angular-Single-Page-Application ausliefert. Ein
Maven-Modul mit Backend und Frontend (via Quinoa).

- **groupId**: `ch.css.demo`, **artifactId**: `caseflow`
- Ziel: Erfassung, Zuweisung, Bearbeitung und Abschluss von Fällen an einem
  Ort; Wiedervorlagen und Fälligkeiten werden aktiv nachverfolgt; jede
  Statusänderung ist im Audit-Trail belegbar; rollenbasierter Zugriff, auch für
  Gesundheitsdaten aus KVG-Fällen.
- Spezifikation unter `specs/` (`high-level-specification.md`,
  `caseflow-arc42.md`, ADRs in `specs/adr/`). `Case` ist das zentrale
  Aggregate Root.

**Status:** Projektskelett. Domäne, Application-Services und Persistenz sind
noch leer — nur Auth (`/api/v1/me`), Version (`/api/v1/version`) und die
App-Shell existieren. Jede User Story bringt ihr Aggregat, ihre Migration und
ihre Tests mit.

## Tech Stack

- **Backend**: Java 25, Quarkus 3.37, Hibernate ORM mit Panache, PostgreSQL, Liquibase
- **Frontend**: Angular 22 (Standalone Components, Signals), TypeScript 6, TailwindCSS 4 — eine SPA, **keine** PWA
- **Auth**: OIDC via Keycloak (BFF-Pattern) — konfiguriert, in dev/test deaktiviert
- **Build**: Maven (mvnw), npm, Quinoa
- **Testing**: JUnit 5 + REST-assured + Mockito + Cucumber (Backend), Vitest + Cucumber.js/Playwright (Frontend), ArchUnit

## Architektur

Hexagonale Architektur (Ports & Adapters) für die Gesamtstruktur, taktisches
DDD für das innere Hexagon. Hexagonal definiert *wo* Code lebt, DDD *welche
Form* der Domänencode hat.

### Backend (Java)

```
ch.css.demo.caseflow/
├── domain/                    # Inneres Hexagon — reine Business-Logik, keine Framework-Imports
│   ├── model/                 # Aggregates, Entities, Value Objects
│   ├── event/                 # Domain Events (erst bei Bedarf anlegen)
│   ├── service/               # Domain Services (erst bei Bedarf anlegen)
│   ├── factory/               # Factories (erst bei Bedarf anlegen)
│   └── port/
│       ├── in/                # Driving Ports (Use-Case-Interfaces)
│       └── out/               # Driven Ports (Repository-Interfaces, Event-Publisher)
├── application/               # Application Services — Use-Case-Orchestrierung, Transaktionsgrenze
│   └── service/
└── adapter/
    ├── in/
    │   └── rest/              # REST-Resources (JAX-RS) — Driving Adapters
    │       ├── dto/           # Transportobjekte der REST-Schicht
    │       └── security/      # Rollen, Dev-Identität, Keycloak-Rollen-Mapping
    └── out/
        └── persistence/       # JPA-Entities, Panache-Repositories — Driven Adapters
```

**Regeln:**
- `domain/` hat **null** Framework-Abhängigkeiten (kein Quarkus, kein JPA, keine Jackson-Annotationen)
- Domänenmodelle sind Java Records; JPA-Entities leben in `adapter/out/persistence/`
- Use-Case-Interfaces (`port/in/`) definieren die API, die Adapter aufrufen
- Repository-Interfaces (`port/out/`) definieren, was die Domäne braucht — implementiert von Persistence-Adaptern
- Adapter hängen von der Domäne ab, nie umgekehrt

Architektur-Invarianten werden via ArchUnit geprüft
(`src/test/java/ch/css/demo/caseflow/architecture/HexagonArchitectureTest.java`).
Der Build bricht, sobald Domain Framework-Imports erhält, Application auf
Adapter zugreift, der Persistence-Adapter andere Adapter referenziert,
REST-DTOs ausserhalb von `adapter/in/rest/dto/` landen oder der REST-Adapter
Domain-Services direkt aufruft.

### DDD Tactical Design

Die Bausteine gehören ins innere Hexagon (`domain/` + `application/`). KISS:
`event/`, `service/` und `factory/` werden erst angelegt, wenn ein echter Use
Case sie braucht.

| Baustein                | Ort                                                       | Zweck & Regeln |
|-------------------------|-----------------------------------------------------------|----------------|
| **Aggregate Root**      | `domain/model/`                                           | Konsistenz- und Transaktionsgrenze. **Einziger** Einstiegspunkt ins Aggregat; Aussenwelt berührt innere Entities nie direkt. |
| **Entity**              | `domain/model/`                                           | Stabile Identität über den Lebenszyklus. Lebt im Aggregat; Gleichheit per Identität. |
| **Value Object**        | `domain/model/`                                           | Immutable, identitätslos, nur durch Attribute definiert. Java `record`; Invarianten im Compact Constructor. Gleichheit per Wert. |
| **Domain Event**        | `domain/event/`                                           | Immutable `record`, Vergangenheitsform (`CaseAssigned`). Publiziert über einen Driven Port. |
| **Domain Service**      | `domain/service/`                                         | Zustandslose Domänenlogik, die zu keiner Entity/VO passt oder mehrere Aggregate umspannt. Pure — kein Framework, keine Persistenz. |
| **Repository**          | Port `domain/port/out/`, Impl `adapter/out/persistence/`  | Collection-Abstraktion für **ein** Aggregate Root. Nimmt/liefert Domänenmodelle, nie JPA-Entities. |
| **Factory**             | `domain/factory/`                                         | Komplexe Aggregat-Erzeugung, die einen Konstruktor übersteigt. |
| **Application Service** | `application/service/`                                    | Orchestriert einen Use Case: laden, Domänenmethode, persistieren, Event publizieren. Transaktionsgrenze — **keine** Geschäftsregeln. |

**Regeln:**
- Andere Aggregate **per ID** referenzieren, nie per Objektreferenz — Aggregate klein halten.
- Ein Repository pro Aggregate Root; ganze Aggregate laden/speichern, keine inneren Entities.
- Invarianten werden **im** Aggregat durchgesetzt, nicht im Application Service.
- Application Service = Orchestrierung; Domain Service = Domänenlogik — getrennt halten.
- Domänenmodelle bleiben immutable Records: "Mutation" liefert eine neue Instanz.
- Jede Statusänderung am Aggregat erzeugt einen Audit-Eintrag — der Audit-Trail ist Teil der Domäne, kein Logging-Nebeneffekt.

### Frontend (Angular)

```
src/main/webapp/src/app/
├── core/                      # Domain- + Application-Schicht
│   ├── models/                # Domain-Interfaces (user.model.ts, version.model.ts, …)
│   ├── services/              # Use-Case-Logik, REST-Zugriff (auth.service.ts, …)
│   ├── guards/                # Route Guards (Komfort-Schicht, Durchsetzung serverseitig)
│   └── interceptors/          # HTTP-Interceptors (BFF-Login-Redirect)
├── features/                  # Driving Adapters — UI-Komponenten pro Route
│   ├── home/
│   └── no-access/
└── shared/                    # Wiederverwendbare UI (app-header, app-footer)
```

DDD tactical design ist ein **Backend**-Thema. Die Interfaces in `core/models/`
spiegeln die REST-DTOs (die veröffentlichte Sprache des Backends), **nicht**
das volle Domänenmodell — sie tragen keine Invarianten und keine Aggregatregeln.

## Rollenmodell

| Rolle          | Zweck |
|----------------|-------|
| `CASE_MANAGER` | Fälle erfassen, übernehmen, bearbeiten, Wiedervorlagen setzen, abschliessen |
| `TEAM_LEAD`    | Fälle zuweisen, Fristen und Auslastung des Teams überwachen |
| `ADMIN`        | Stammdaten, Vertretungen, Konfiguration |
| `AUDITOR`      | Lesender Zugriff auf Audit-Trail und Reports |

Interne Namen in `adapter/in/rest/security/Roles.java`; Keycloak-Client-Rollen
(`caseflow-case-manager`, …) werden vom `RoleMappingAugmentor` abgebildet.
Gesundheitsdaten (KVG) sind besonders schützenswert: Owner-/Rollen-Checks
gehören **immer** ins Backend, das Frontend blendet nur aus.

## Build & Run

```bash
./mvnw quarkus:dev          # Dev-Modus mit Hot-Reload (Java + Angular)
./mvnw package              # Production-Build → target/quarkus-app/quarkus-run.jar
./mvnw package -Dnative     # GraalVM Native Image
scripts/dev-start.sh        # Podman-Setup + Dev-Modus (--auth keycloak für echten Login)
```

Frontend-Dev-Server auf :4200, Quarkus auf :8080 proxied dorthin.

## Datenbank

- PostgreSQL mit Liquibase-Migrationen (`quarkus.liquibase.migrate-at-start=true`)
- Hibernate im Validate-Modus — Liquibase verwaltet das Schema, Hibernate validiert nur
- Dev Services starten in dev/test automatisch einen PostgreSQL-Container (Docker/Podman nötig)

## Testing

```bash
./mvnw test                       # Backend-Unit-Tests (Surefire) — ohne Container
./mvnw verify                     # + @QuarkusTest-ITs, Cucumber, Frontend-Vitest via Quinoa
cd src/main/webapp && npm test    # Nur Frontend-Tests (Vitest)
```

- **Unit-Tests** (`*Test`): Domänenlogik und Use-Case-Services isoliert — Driven Ports mocken
- **`@QuarkusTest`** (`*IT`): startet die App gegen Dev-Services-PostgreSQL — braucht eine Container-Runtime
- **Cucumber**: `.feature`-Dateien unter `src/test/resources/features/`; Java-Runner `CucumberIT` (ohne Tag), Playwright-Runner via `npm run e2e:cucumber` (Tag `@E2E`)
- **Guardrails** (`*Test`, ohne Container): `HexagonArchitectureTest` (Schichtdisziplin), `SpecScenarioParityTest` (Story ↔ `.feature`-Szenarien synchron), `DisplayTextTransliterationTest` (keine ASCII-Transliteration in kundensichtbarem Angular-Text)
- Verhalten testen, nicht Implementierung. Eine Assertion pro logischem Konzept.

## Clean Code

### Prinzipien

- **Single Responsibility**: Jede Klasse/Komponente tut eine Sache. Services orchestrieren, Adapter konvertieren, Domänenmodelle halten Zustand.
- **Dependency Inversion**: Domäne definiert Interfaces (Ports), Adapter implementieren sie. Abhängigkeiten zeigen immer nach innen.
- **KISS**: Einfachste funktionierende Lösung. Keine spekulativen Abstraktionen.
- **DRY**: Erst extrahieren, wenn Duplikation real ist (3+ Vorkommen).
- **Fail Fast**: An Systemgrenzen validieren (REST-Endpoints, User-Input). Internem Code vertrauen.

### Naming

- Klassen beschreiben **was sie sind**, Methoden **was sie tun**
- Keine Abkürzungen ausser etablierten (ID, URL, DTO, HTTP)
- Java: `CaseService`, `CaseRepository`, `CaseEntity`, `CaseResource`
- Angular: `case-list.component.ts`, `case.service.ts`, `case.model.ts`
- DB-Spalten: snake_case

**DDD-Bausteine** — ubiquitäre Sprache der Domäne, keine technischen Suffixe:
- Aggregate Roots, Entities, Value Objects: das Domänen-Nomen (`Case`, `Deadline`, `Assignment`), kein `*Aggregate`/`*VO`-Suffix
- Domain Events: Vergangenheitsform (`CaseAssigned`, `CaseClosed`)
- Repositories (Driven Ports): `<AggregateRoot>Repository` (`CaseRepository`)
- Domain Services: `<Capability>Service` nur, wenn keine Entity/VO passt
- Factories: `<AggregateRoot>Factory`

### Dokumentation

- **Nur öffentliche APIs dokumentieren** — public Methoden auf Ports, Services, Resources
- Kein Javadoc auf privaten Methoden, Gettern, Settern oder selbsterklärendem Code
- **Warum** dokumentieren, nie **was** — Kommentare erklären nicht-offensichtliche Entscheidungen
- Keine `@author`-Tags, keine Change-Logs im Code — git hält die Historie

### Code-Qualität

- Kein toter Code, kein auskommentierter Code, keine TODOs ohne Issue-Referenz
- Keine Magic Numbers — benannte Konstanten
- Early Returns statt tiefer Verschachtelung
- Keine `null`-Rückgaben — `Optional` (Java) bzw. `undefined`/Union-Types (TypeScript)

## Code-Konventionen

### Java (Backend)

- **Records** für Domänenmodelle (Aggregates, Entities, Value Objects), Domain Events und DTOs — immutable by default
- **Value Objects** validieren Invarianten im Compact Constructor — fail fast
- **JPA-Entity-Klassen** nur in `adapter/out/persistence/`, public Fields OK — nicht mit DDD-Entities verwechseln
- Panache-Repository-Pattern (`implements PanacheRepositoryBase<T, ID>`) — der Adapter hinter einem `port/out/`-Repository
- `@ApplicationScoped` für alle Services und Repositories
- Constructor Injection (Testbarkeit)

### Angular (Frontend)

- **Standalone Components** — nie NgModules. `standalone: true` NICHT setzen (Default seit Angular v20+)
- **Signals** für reaktiven State: `signal()`, `computed()`, `effect()`
- `inject()` für DI — nie Constructor Injection
- `input()` und `output()` statt `@Input`/`@Output`
- `ChangeDetectionStrategy.OnPush` auf allen Komponenten
- Nativer Control Flow: `@if`, `@for`, `@switch` — nie `*ngIf`, `*ngFor`
- `providedIn: 'root'` für Singleton-Services
- TypeScript strict — kein `any`, `unknown` bei unsicherem Typ
- Feature-basierte Ordnerstruktur, `app-*`-Selector-Prefix

### Sprache

- Deutsch mit korrekten Umlauten (kein `loeschen`), Schweizer Hochdeutsch ohne `ß`, Du-Form
- **Korrekte Umlaute in allem für Menschen Lesbaren**: Fliesstext, Kommentare, Javadoc, Commit-Messages, PR-Beschreibungen und kundensichtbare Strings.
- **ASCII-Transliteration (`ae`/`ue`/`oe`/`ss`) NUR in Identifiern**, die per Konvention ASCII-only bleiben: Java-Enum-Konstanten, JSON-Keys, Cucumber-Tags, Branch-Namen. `Stoss` (ohne ß) ist korrektes Schweizer Hochdeutsch, keine Transliteration.
- **Achtung Leak**: Die ASCII-Schreibweise eines Identifiers darf nicht in den abgeleiteten Anzeige-Text wandern.

### Formatierung

- **Frontend**: Prettier (print width 100, single quotes), EditorConfig (2 Spaces, UTF-8)
- **Backend**: Java-Standardkonventionen, 4 Spaces

## Quarkus-Profile

| Profil      | Zweck |
|-------------|-------|
| `%dev`      | Quinoa-Dev-Server, Dev-Services-DB, OIDC aus, Dev-Identität `CASE_MANAGER` |
| `%test`     | Random HTTP-Port, kein Quinoa-Dev-Server, OIDC aus |
| `%keycloak` | OIDC mit Keycloak (kombinieren: `-Dquarkus.profile=dev,keycloak`) |
| `%postgres` | Direkte PostgreSQL-Verbindung (localhost:5434) |
| `%prod`     | OIDC an, explizite DB-URL, Security-Header |

## Wiederkehrende Aufgaben

- **Aggregat hinzufügen**: Aggregate Root (+ Entities/Value Objects) als Records in `domain/model/`, Repository-Port in `domain/port/out/`, Use-Case-Port in `domain/port/in/`, Application Service in `application/service/`, JPA-Entity + Panache-Repository in `adapter/out/persistence/`, REST-Resource + DTOs in `adapter/in/rest/`, Liquibase-Migration in `db/migration/` + Include in `db/changeLog.xml`
- **Domain Event hinzufügen**: Record in `domain/event/`, Publisher-Port in `domain/port/out/`, aus dem Application Service publizieren, nachdem das Aggregat persistiert ist
- **Domain Service hinzufügen**: Pure Klasse in `domain/service/` — nur wenn die Logik zu keiner Entity/VO passt
- **Frontend-Feature hinzufügen**: Komponente in `features/`, Route in `app.routes.ts`, Model in `core/models/`, Service in `core/services/`, Nav-Link in `shared/app-header.component.html`
- **Rolle hinzufügen**: Konstante in `Roles.java`, Mapping im `RoleMappingAugmentor`, Client-Rolle + Testuser in `keycloak/caseflow-realm-dev.json`, `Role`-Union in `core/models/user.model.ts`, Policy `staff` in `application.properties` prüfen
- **User Story hinzufügen**: `specs/user-stories/<nr>-<slug>.md` + Zeile in `specs/user-stories/README.md` + Nav-Eintrag in `mkdocs.yml`; `.feature` unter `src/test/resources/features/<bereich>/`. Feature-Titel `US-<nr> <Kurztitel>` — `SpecScenarioParityTest` erzwingt gleiche Szenario-Anzahl in Story und Feature.
- **Architekturentscheid festhalten (ADR)**: neuer Record `specs/adr/adr-<NN>-<slug>.md` (Format Kontext → Entscheidung → Begründung → Konsequenzen → Verworfene Alternativen) + Zeile im Index `specs/adr/README.md` + Nav-Eintrag in `mkdocs.yml`. Skill `/adr` automatisiert das.

## Copilot-Werkzeuge in diesem Repo

| Ort | Zweck |
|-----|-------|
| `.github/instructions/*.instructions.md` | Pfadbezogene Regeln (Backend-Hexagonal, Angular, Auth/Security, Cucumber) — greifen automatisch beim Bearbeiten passender Dateien |
| `.github/agents/*.agent.md` | Custom Agents `hexagonal-reviewer`, `angular-signals-reviewer`, `auth-security-reviewer`, `bdd-cucumber-author` — als Reviewer vor jedem Commit einsetzen |
| `.github/skills/<name>/SKILL.md` | Agent Skills (agentskills.io-Standard) `task`, `plan`, `implement`, `ship`, `autoship`, `fast`, `fix-e2e`, `adr` — nur auf ausdrücklichen Aufruf ausführen |
| `.github/skills/_shared/*.md` | Geteilte Fragmente (DoD, BDD, Kosten-Disziplin, MCP-Erkennung), aus den Skills referenziert |
| `.github/prompts/*.prompt.md` | Dünne Prompt-Dateien für VS Code (`/task`, `/implement`, …), delegieren an den gleichnamigen Skill |
| `.github/hooks/reviewer-reminder.json` | PostToolUse-Hook: erinnert nach Edits an den passenden Reviewer-Agenten |

Nach Änderungen an Backend-, Angular-, Auth- oder Feature-Dateien den passenden
Reviewer-Agenten laufen lassen und gemeldete Verstösse vor dem Commit beheben.
