---
name: implement
description: "Setzt ein Issue um: Branch oder Worktree anlegen, implementieren, Reviewer-Agenten, Tests, PR erzeugen. Nur auf ausdrücklichen Aufruf (/implement) ausführen, nie eigenständig aktivieren."
compatibility: "Requires git, gh (GitHub CLI) oder einen Git-Provider-MCP, Maven (mvnw/mvnd), Node/npm"
metadata:
  argument-hint: "<issue-nummer> [--worktree]"
---

Setze das im Aufruf genannte Issue (`#<nr>`) um. Bei `--worktree`: isolierter Git-Worktree statt Feature-Branch.

## Cost & Token Discipline

Bevor du anfängst: lies `.github/skills/_shared/cost-discipline.md`. Die Regeln
dort (Read-once, Suche vor Volltext, Todo sparsam, Terminal-Output filtern,
Conditional Reviewer, Setup-Subagent) gelten für **alle** Schritte unten.

## 1. MCP-Server ermitteln

Lies `.github/skills/_shared/mcp-detection.md`.

## 2–4. Setup (Subagent)

Die mechanischen Setup-Schritte (Issue lesen + zuweisen, Branch/Worktree
anlegen, User-Story-Datei mit Fall A/B/C) an einen Subagent delegieren —
Vorlage und Prompt-Template stehen im Abschnitt „Setup-Subagent" von
`.github/skills/_shared/cost-discipline.md`. Spart typisch 15–25
Tool-Aufrufe im Hauptkontext.

Bei Konflikten zwischen Stories (Fall A): stillere, konsistentere
Variante wählen und im PR-Body unter „Story-Konsistenz" begründen — **nicht**
beim User nachfragen.

## 5. Plan-Gate

Bevor du Code schreibst: prüfe, ob für dieses Issue ein **bestätigter Plan** unter
`specs/plans/us-<nr>-*.md` existiert.

- **Plan vorhanden** → daran orientieren, weiter zu Schritt 5a.
- **Plan fehlt und das Issue ist nicht-trivial** → erst `/plan` mit dieser
  Issue-Nummer ausführen und Bestätigung einholen, dann fortfahren. Als
  nicht-trivial gilt jede dieser Heuristiken:
  - Use-Case-Issue mit **mehr als 3 Akzeptanzkriterien**
  - berührt **mehr als eine Schicht** (z. B. Domain + Adapter + Frontend)
  - berührt **Migration (Liquibase), Auth/Security, Audit-Trail** oder Breaking Changes
- **Trivialer Task** (einzelne Datei, keine der obigen Punkte) → ohne Plan direkt
  weiter.

## 5a. Implementieren

- **Lay of the Land erst:** Vor der eigentlichen Implementierung **einen** Subagent (z. B. `runSubagent`/Explore-Agent) starten, der die für das Issue relevanten Dateien (Aggregates, Ports, Adapter, REST-Resources, Tests, Specs) lokalisiert und kurz beschreibt. Spart 10–20 sequenzielle find/grep-Aufrufe.
- Halte dich an `.github/copilot-instructions.md` und die pfadbezogenen Regeln in `.github/instructions/` (Hexagonal, DDD, Signals, OnPush, …).
- Falls `/plan` lief: am bestätigten Plan in `specs/plans/` orientieren.
- Schreibe Tests parallel zur Feature-Implementierung (Unit + Komponenten).

## 6. BDD umsetzen (nur US-Issues)

Lies und befolge `.github/skills/_shared/bdd-requirements.md`. Nutze den Agenten `bdd-cucumber-author` bei Unsicherheit zum Tag-Routing.

## 7. Reviewer-Runde

Nach der Implementierung und vor dem Commit. Die Reviewer sind Custom Agents in `.github/agents/` und voneinander unabhängig — wo möglich **parallel** als Subagents starten, nicht nacheinander.

**Conditional — Reviewer nur aufrufen, wenn relevante Dateien im Diff:**
```bash
git diff --name-only origin/main
```
Ergebnis auswerten und nur die Matches starten:

- `hexagonal-reviewer` — nur wenn `src/main/java/**` im Diff
- `angular-signals-reviewer` — nur wenn `src/main/webapp/src/app/**` im Diff
- `auth-security-reviewer` — nur wenn `*Resource.java`, `oidc`, `application.properties` oder Security-Filter im Diff
- `bdd-cucumber-author` — nur wenn `.feature`-Dateien neu/geändert

Der Hook `.github/hooks/reviewer-reminder.json` weist dich nach Edits auf die passenden Agenten hin. Gemeldete Verstösse direkt fixen und Reviewer ggf. erneut laufen lassen, bevor der Commit erfolgt.

## 8. Definition of Done

Abgleichen mit `.github/skills/_shared/dod-checklist.md`. Alle anwendbaren Punkte erfüllt? Sonst zurück zu 5–7.

## 9. Commit

- Gezielt stagen (kein `git add -A`)
- Message: `<typ>(<scope>): <Beschreibung> (#<nr>)` — Deutsch mit korrekten Umlauten

## 10. Build & Tests — Fail-Fast-Loop

**Build-Befehl: `mvnd`** wenn im PATH (warmer Maven-Daemon, spart pro Aufruf ca. 30 s JVM-Startup). Sonst `./mvnw`.

1. **Backend Unit + Integration in einem Run** —
   ```bash
   mvnd verify -Dsurefire.skipAfterFailureCount=1 -Dfailsafe.skipAfterFailureCount=1
   ```
2. **Frontend Unit** — `cd src/main/webapp && npm test -- --bail`
3. **E2E nur bei UI-/Feature-Touch.** Heuristik:
   ```bash
   git diff --name-only origin/main -- \
     src/main/webapp/src/app/ \
     src/main/webapp/src/assets/ \
     src/test/resources/features/
   ```
   - Leer → E2E überspringen, im PR-Body unter „Tests" notieren (`E2E skipped: kein UI-/Feature-Touch`).
   - Sonst → `cd src/main/webapp && npm run e2e:cucumber -- --fail-fast`.

**Kein** finales `mvn clean install` mehr — Schritte 1+2 haben Unit + Integration bereits validiert.

Ersten Fehler fixen, Suite ab Schritt 1 neu starten, bis grün.

## 11. Push & PR

- `git push -u origin feature/<nr>-<slug>`
- PR via MCP (oder `gh pr create`):
  - Titel: Issue-Titel
  - Body: `Closes #<nr>` + Zusammenfassung, Struktur gemäss `.github/pull_request_template.md`
  - Base: `main`
- **Worktree:** nicht zurückwechseln, `/ship` räumt auf.
- **Kein Worktree:** auf Feature-Branch bleiben.
