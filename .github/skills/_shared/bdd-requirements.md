# Shared Fragment: BDD-Umsetzung (nur US-Issues)

Pflicht für jede User Story. `@Pending` ist **kein** Abschluss-Zustand.

## Runner-Entscheidung

- **Java-Cucumber-Runner** — REST-API testbar: Fall-CRUD, Zuweisung, Fristen, Auth, Persistenz. Kein `@E2E`-Tag.
- **Playwright (Cucumber.js)** — Browser nötig: Fallerfassungs-Formular, Fallübersicht, Navigation. Tag `@E2E`.
- **Beide** — Story in Backend- und Frontend-Anteil zerlegen.
- **Nur Vitest** — reine Parser-/Berechnungs-/Utility-Funktionen. Keine `.feature`-Datei, Vitest-Abdeckung in der User Story dokumentieren.

## Umsetzung

1. **Feature-Datei** unter `src/test/resources/features/<unterordner>/` auf Deutsch (`# language: de`), Titel `US-<nr> <Kurztitel>`. Szenarien = User-Story-Datei.
2. **Step-Definitions**:
   - Java: `src/test/java/ch/css/demo/caseflow/cucumber/steps/*Steps.java`, REST-assured
   - Playwright: `src/main/webapp/e2e/cucumber/steps/*.steps.ts`, Selektoren via `aria-label`/`data-testid`
   - Gemeinsame Steps (`CommonSteps.java`, `common.steps.ts`) wiederverwenden, nicht duplizieren
3. **Lokal verifizieren**:
   - Java: `./mvnw test -Dtest=CucumberIT`
   - Playwright: `./mvnw verify` (oder Debug: `cd src/main/webapp && npm run e2e:cucumber`)
4. **`@Pending` entfernen** — das Feature muss real laufen.

Bei Reviewer-Bedarf: Agent `bdd-cucumber-author` (`.github/agents/`) aufrufen.
