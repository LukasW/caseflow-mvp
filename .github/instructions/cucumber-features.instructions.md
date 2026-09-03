---
applyTo: "**/*.feature"
---

# Cucumber-Features — Tag-Routing

Beide Runner (JVM + Cucumber.js/Playwright) teilen sich die `.feature`-Dateien unter
`src/test/resources/features/`. Der Tag entscheidet, wer ausführt. Bei Unsicherheit den
Custom Agent `bdd-cucumber-author` einsetzen.

| Tag | Runner | Wann |
|-----|--------|------|
| _kein Tag_ oder `@Backend` | Java-Cucumber (`CucumberIT`, REST-assured) | Ohne Browser reproduzierbar: Fall-CRUD, Zuweisung, Fristen, Auth, Persistenz |
| `@E2E` | Cucumber.js + Playwright (`npm run e2e:cucumber`) | Browser nötig: Formulare, Fallübersicht, Navigation |
| `@Pending` | keiner | Spezifiziert, nicht implementiert — **kein Endzustand** einer User Story |

## Regeln
- `# language: de` als erste Zeile; Feature-Titel `US-<nr> <Kurztitel>`
- Gherkin auf Deutsch: Angenommen / Wenn / Dann / Und; Struktur Als/möchte/damit
- `Hintergrund` nur für wiederkehrendes Setup
- Szenariotitel beschreiben Verhalten, nicht Implementierung
- Keine technischen Details (DB, HTTP-Statuscodes nur in Backend-Features)
- Szenarien isoliert und idempotent
- Bestehende Steps wiederverwenden: Java `src/test/java/ch/css/demo/caseflow/cucumber/steps/**`,
  Playwright `src/main/webapp/e2e/cucumber/steps/**`; gemeinsame Steps (`CommonSteps.java`,
  `common.steps.ts`) nicht duplizieren
- Bei `@Pending`: Kommentar mit Issue-Referenz
- Lokal prüfen: Java `./mvnw test -Dtest=CucumberIT`, Playwright `./mvnw verify`
