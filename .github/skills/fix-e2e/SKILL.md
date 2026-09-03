---
name: fix-e2e
description: "Bringt iterativ alle E2E-Tests grün: Maven fail-fast starten, scheiternden Test flicken, neu starten, bis der komplette Build durchläuft. Nur auf ausdrücklichen Aufruf (/fix-e2e) ausführen, nie eigenständig aktivieren."
compatibility: "Requires Maven (mvnw), Node/npm, Playwright-Browser; Playwright-MCP optional"
---

Ziel: `./mvnw clean install` komplett grün bekommen, inklusive der
Cucumber-/Playwright-E2E-Tests.

## Ablauf

Wiederhole diese Schleife, bis der komplette Build grün durchläuft:

1. **Starten**: `./mvnw clean install -Dsurefire.skipAfterFailureCount=1 -Dfailsafe.skipAfterFailureCount=1` im Hintergrund-Terminal. Der Fail-Fast-Mechanismus sorgt dafür, dass Maven beim ersten gescheiterten IT abbricht.

2. **Fail-Fast für Cucumber sicherstellen**: Die Cucumber.js-Konfiguration (`src/main/webapp/cucumber.json`) muss `failFast: true` gesetzt haben, damit Cucumber.js ebenfalls beim ersten gescheiterten Szenario stoppt.

3. **Beobachten**: Während der Build läuft, ist die Ausgabe gepuffert. Zwischenstand via `target/failsafe-reports/` und `target/quarkus.log` (relativ zum Projekt-Root) prüfen.

4. **Report**: Sobald der Build abbricht (oder fertig ist), gib dem User:
   - **Welche Tests sind durchgelaufen?** (aus den `[INFO] Tests run: N, Failures: 0, Errors: 0` Zeilen)
   - **Welcher Test ist gescheitert?** (Klassenname + Testmethode oder Cucumber-Szenarioname)
   - **Art des Fehlers**: Timeout, Assertion-Failure, Selector-Violation, etc.
   - **Fehlermeldung**: Die relevanten Zeilen aus dem Stacktrace

5. **Flicken**: Analysiere den Fehler und flicke **ausschliesslich** den gescheiterten Test (bzw. die Step-Definition / den zugehörigen Production-Code).
   - Bei Playwright-Timeout: Locator prüfen (exact-match, role-based, stabile Selektoren), auf Race-Conditions mit dem Angular-Bootstrap achten.
   - Bei Assertion-Failure: Erwartungswert prüfen, ggf. an die tatsächliche UI-Struktur anpassen.
   - Bei Strict-Mode-Violations: Locator engeren Scope geben, `{ exact: true }` nutzen, oder `.first()` / `.nth(n)`.
   - **Nicht** andere (grüne) Tests mitändern — nur den einen, der gerade failed.

6. **Neu starten**: Zurück zu Schritt 1. Wenn derselbe Test wieder mit demselben Fehler failed → tiefer analysieren (Seite per Playwright-MCP anschauen, HTTP-Log prüfen). Wenn ein neuer Test failed → normaler nächster Iterationsschritt.

## Nützliche Debug-Shortcuts

- **Cucumber einzeln gegen laufenden Quarkus**: Quarkus standalone starten (`java -jar target/quarkus-app/quarkus-run.jar` mit dem E2E-Profil und deaktiviertem OIDC), dann Cucumber.js direkt gegen `http://localhost:8080` laufen lassen — viel schneller als ein voller `./mvnw verify`.
- **Live-Inspection via Playwright-MCP**: `browser_navigate` + `browser_evaluate` gegen den laufenden Quarkus zeigt den tatsächlichen DOM-Zustand.
- **Screenshots aus dem Cucumber-Report**: PNG-Attachments aus dem HTML-Report dekodieren und als Bild ansehen.

## Commit & Abschluss

Nur dann einen Commit erstellen, wenn der komplette `./mvnw clean install` ohne
Fail-Fast-Flags grün durchläuft. Commit-Message: `fix(e2e): <kurze
Beschreibung> (#<issue>)`. Nicht pushen ohne explizite User-Freigabe.
