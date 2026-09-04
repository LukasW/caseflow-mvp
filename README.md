# CaseFlow

Case-Management-Tool für die CSS: ein Quarkus Backend-For-Frontend, das eine
Angular-Single-Page-Application (SPA) ausliefert.

- **groupId**: `ch.css.demo`
- **artifactId**: `caseflow`

## Warum

Case Manager jonglieren heute zwischen E-Mail, Excel und dem Kernsystem. Fälle
gehen unter, Fristen werden knapp, niemand hat den vollen Überblick.

**Ein Tool. Ein Fall. Volle Kontrolle.**

- **Alles an einem Ort**: Erfassung, Zuweisung, Bearbeitung, Abschluss — kein Wechsel zwischen Systemen mehr.
- **Keine verpassten Fristen**: Wiedervorlagen und Fälligkeiten werden aktiv nachverfolgt, nicht in einer Excel-Liste vergessen.
- **Lückenlose Nachvollziehbarkeit**: Jede Statusänderung protokolliert — für Audit und Compliance jederzeit belegbar.
- **Gebaut für sensible Daten**: Rollenbasierter Zugriff von Anfang an, auch für Gesundheitsdaten aus KVG-Fällen.
- **Schnell startklar**: MVP in wenigen Wochen einsatzbereit, ohne auf die grosse Kernsystem-Integration zu warten.

Die fachliche und architektonische Spezifikation lebt unter `specs/`:

- `specs/high-level-specification.md` — fachliche Spezifikation und MVP-Scope
- `specs/caseflow-arc42.md` — arc42-Zielarchitektur
- `specs/adr/` — Architecture Decision Records
- `specs/user-stories/` — User Stories und Traceability

**Status:** Projektskelett — Auth, App-Shell, Build- und Test-Infrastruktur
stehen; die Fachdomäne entsteht Story für Story.

## Tech Stack

- **Backend**: Java 25, Quarkus 3.37, Hibernate ORM with Panache, PostgreSQL, Liquibase
- **Frontend**: Angular 22 (standalone components, signals), TypeScript 6, TailwindCSS 4 — a plain SPA (no PWA)
- **Auth**: OIDC via Keycloak (BFF pattern) — configured but disabled in dev/test
- **Build**: Maven (`mvnw`), npm, Quinoa integration
- **Testing**: JUnit 5 + REST-assured + Mockito + Cucumber (backend), Vitest + Playwright (frontend), ArchUnit

## Prerequisites

- JDK 25
- Node.js ≥ 22.22 and npm 11 for direct `npm` calls in `src/main/webapp` (see `.nvmrc`);
  Quinoa installs its own managed copy for the Maven build, so `./mvnw verify` works without it
- A running Docker/Podman daemon — Quarkus Dev Services starts a PostgreSQL
  container for `dev`, `test` and the `@QuarkusTest` integration tests

## Build & Run

```bash
./mvnw quarkus:dev          # Dev mode with hot-reload (Java + Angular), http://localhost:8080
./mvnw package              # Production build → target/quarkus-app/quarkus-run.jar
./mvnw package -Dnative     # GraalVM native image
```

Run the packaged app:

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

### Lokaler Schnellstart

`scripts/dev-start.sh` fährt die App im Dev-Modus hoch und kapselt die
Container-Runtime-Vorbereitung (Podman auf Apple-Silicon-Maschinen).

```bash
scripts/dev-start.sh                 # OIDC aus, Rolle CASE_MANAGER
scripts/dev-start.sh --role lead     # Ersatzidentität TEAM_LEAD
scripts/dev-start.sh --auth keycloak # echter OIDC-Login über lokalen Keycloak
```

Mit `--auth keycloak` startet das Skript zusätzlich einen Keycloak-Container auf
`:8180`, importiert den Realm aus `keycloak/caseflow-realm-dev.json` und fährt
die App im Profil `dev,keycloak` hoch. Testuser (Passwort = Benutzername):
`case-manager`, `team-lead`, `admin`, `auditor`. `--help` zeigt alle Flags.

### Dev-Identität wechseln

Im `dev`-Profil ist OIDC deaktiviert; die `DevAuthenticationMechanism` stellt
stattdessen eine feste Identität bereit (Defaults in `application.properties`):

```properties
%dev.caseflow.auth.dev-user=dev
%dev.caseflow.auth.dev-role=CASE_MANAGER
```

```bash
./mvnw quarkus:dev -Dcaseflow.auth.dev-user=lead -Dcaseflow.auth.dev-role=TEAM_LEAD
```

Für das wiederkehrende Durchspielen als Team Lead (z. B. Fälle zuweisen und
umverteilen) gibt es das Profil `teamlead`, das über `dev` gelegt wird und nur
die Rolle überschreibt (rechtes Profil gewinnt):

```bash
./mvnw quarkus:dev -Dquarkus.profile=dev,teamlead
```

Die aktive Identität lässt sich jederzeit über `GET /api/v1/me` überprüfen.

## Testing

```bash
./mvnw test                                # Backend unit tests (no container needed)
./mvnw verify                              # + @QuarkusTest ITs, Cucumber, frontend Vitest
cd src/main/webapp && npm test             # Frontend unit tests (Vitest) only
cd src/main/webapp && npm run e2e:cucumber # Playwright E2E (@E2E) against http://localhost:8080
```

## Hexagonal Architecture

```
ch.css.demo.caseflow/
├── domain/                 # Pure business logic — no framework imports
│   ├── model/              # Case, … (records)
│   └── port/
│       ├── in/             # Use-Case-Interfaces (driving ports)
│       └── out/            # Repository-Interfaces, Gateways (driven ports)
├── application/
│   └── service/            # Application services — implement the use cases
└── adapter/
    ├── in/rest/            # JAX-RS resources — driving adapters
    └── out/persistence     # JPA + Panache — driven adapters
```

See `.github/copilot-instructions.md` for the full hexagonal + DDD tactical-design
conventions.
Architektur-Invarianten werden via ArchUnit geprüft
(`src/test/java/ch/css/demo/caseflow/architecture/HexagonArchitectureTest.java`).

## Quarkus Profiles

| Profile     | Purpose                                                              |
|-------------|----------------------------------------------------------------------|
| `%dev`      | Quinoa dev server, Dev Services PostgreSQL, OIDC disabled             |
| `%test`     | Random HTTP port, no Quinoa dev server, OIDC disabled                |
| `%keycloak` | OIDC enabled (combine with dev: `-Dquarkus.profile=dev,keycloak`)    |
| `%teamlead` | Dev-Ersatzidentität als `TEAM_LEAD` (combine with dev: `-Dquarkus.profile=dev,teamlead`) |
| `%postgres` | Direct PostgreSQL connection on localhost:5434                       |
| `%prod`     | OIDC enabled, explicit DB URL, security headers                      |
