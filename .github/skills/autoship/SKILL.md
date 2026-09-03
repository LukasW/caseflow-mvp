---
name: autoship
description: "Setzt ein Issue von A bis Z um und mergt es: Implementierung, Reviews, PR, CI, Merge, Aufräumen in einem Rutsch ohne Rückfragen. Nur auf ausdrücklichen Aufruf (/autoship) ausführen, nie eigenständig aktivieren."
compatibility: "Requires git, gh (GitHub CLI) oder einen Git-Provider-MCP, Maven (mvnw/mvnd), Node/npm"
metadata:
  argument-hint: "<issue-nummer> [--worktree]"
---

End-to-End-Ablauf für das im Aufruf genannte Issue (`#<nr>`, optional `--worktree`): Branch → Implementierung → Reviews → PR → Merge → Aufräumen. **Ohne Rückfragen oder Pausen.** Bei mehreren Optionen die beste Variante selbst auswählen und anwenden.

## Verhalten bei Entscheidungen

- **Niemals** den User um Bestätigung bitten.
- **Niemals** auf manuelles Diff-Review warten.
- Bei Unklarheiten: kurze Analyse, beste Variante wählen, Entscheidung im Commit/PR-Body kurz begründen.
- Bei Fehlern (Tests, CI, Reviewer-Findings): selbst diagnostizieren, fixen, neu starten — nicht abbrechen.
- Nur abbrechen, wenn echte Blocker auftreten (z. B. fehlende Credentials, externer Dienst dauerhaft down, Liquibase-Konflikt mit Bestandsdaten ohne klare Migration). Dann mit klarem Status zurückmelden.

## Cost & Token Discipline

Bevor du anfängst: lies `.github/skills/_shared/cost-discipline.md`. Die Regeln
dort gelten für **alle** Phasen unten.

## Phase A — Implement (analog `/implement`)

### A1. MCP-Server ermitteln

Lies `.github/skills/_shared/mcp-detection.md`.

### A2–A4. Setup (Subagent)

Die mechanischen Setup-Schritte (Issue lesen + zuweisen, Branch/Worktree
anlegen, User-Story-Datei) an einen Subagent delegieren — Vorlage und
Prompt-Template stehen im Abschnitt „Setup-Subagent" von
`.github/skills/_shared/cost-discipline.md`.

Hinweis Night-Batch: Wenn der aktuelle Worktree bereits einen Branch
`auto/<nr>` hat, diesen wiederverwenden statt `feature/<nr>-<slug>` neu
anzulegen — der Setup-Subagent erkennt das per `git branch --show-current`.

### A5. Implementieren

- **Lay of the Land erst:** Vor der Implementierung **einen** Subagent (z. B. `runSubagent`/Explore-Agent) starten, der die für das Issue relevanten Dateien (Aggregates, Ports, Adapter, REST-Resources, Tests, Specs) lokalisiert und kurz beschreibt.
- Halte dich an `.github/copilot-instructions.md` und `.github/instructions/` (Hexagonal, DDD, Signals, OnPush, …).
- Falls ein `/plan`-Output in `specs/plans/` vorliegt: daran orientieren.
- Tests parallel zur Implementierung schreiben (Unit + Komponenten).

### A6. BDD umsetzen (nur US-Issues)

Lies und befolge `.github/skills/_shared/bdd-requirements.md`. Wenn Tag-Routing unklar: best-fit-Routing selbst entscheiden (REST-API ohne Browser → Java-Cucumber, UI-Flow → Playwright `@E2E`, beides → splitten). Niemals `@Pending` als Endzustand belassen.

### A7. Reviewer-Runde

Vor dem Commit. Reviewer sind Custom Agents in `.github/agents/`, voneinander unabhängig — wo möglich **parallel** als Subagents starten.

**Conditional — Reviewer nur aufrufen, wenn relevante Dateien im Diff:**
```bash
git diff --name-only origin/main
```
Ergebnis auswerten und nur die Matches starten:

- `hexagonal-reviewer` — nur wenn `src/main/java/**` im Diff
- `angular-signals-reviewer` — nur wenn `src/main/webapp/src/app/**` im Diff
- `auth-security-reviewer` — nur wenn `*Resource.java`, `oidc`, `application.properties` oder Security-Filter im Diff
- `bdd-cucumber-author` — nur wenn `.feature`-Dateien neu/geändert

Gemeldete Verstösse direkt fixen und Reviewer ggf. erneut laufen lassen.

### A8. Definition of Done

Abgleichen mit `.github/skills/_shared/dod-checklist.md`. Lücken eigenständig schliessen, bevor commitiert wird.

### A9. Commit

- Gezielt stagen (kein `git add -A`).
- Message: `<typ>(<scope>): <Beschreibung> (#<nr>)`.

### A10. Build & Tests — Fail-Fast-Loop

**Build-Befehl: `mvnd`** wenn im PATH, sonst `./mvnw`.

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

**Kein** finales `mvn clean install` — Schritte 1+2 haben Unit + Integration bereits validiert.

Ersten Fehler fixen, Suite ab Schritt 1 neu starten, bis grün.

### A11. Rebase auf main

Vor dem Push **immer** rebasen:

```bash
git fetch origin main
git rebase origin/main
```

Bei Konflikten **eigenständig lösen** — Kontext verstehen, beide Seiten zusammenführen, Tests neu laufen lassen (A10 wiederholen). Auflösung im PR-Body unter „Rebase-Notes" kurz erwähnen. **Nicht** abbrechen, nicht nachfragen. Nur abbrechen, wenn der Konflikt domänenfremd und ohne Kontext der anderen Änderung nicht lösbar ist.

### A12. Push & PR

- `git push -u origin feature/<nr>-<slug>` (bei wiederholtem Push nach Rebase: `--force-with-lease`).
- **Vor `pr create`** prüfen, ob für den Branch schon ein PR existiert (offen oder gemergt). Falls offen: weiterverwenden. Falls gemergt: Phase B übernimmt das Aufräumen.
- PR via MCP anlegen:
  - Titel: Issue-Titel
  - Body: `Closes #<nr>` + Zusammenfassung + ggf. eigene Entscheidungsbegründungen
  - Base: `main`

## Phase B — Ship (analog `/ship`)

### B1. Branch & Push

- Branch-Namen merken: `git branch --show-current`.
- `git status` — bei uncommitteten Änderungen aus Phase A: nachträglich committen (Message-Stil wie A9), nicht abbrechen.
- `git push` falls noch nicht geschehen.

### B2. DoD & CI

- DoD nochmals abgleichen mit `.github/skills/_shared/dod-checklist.md`.
- **CI-Wait — robust, ohne selbstgebaute Polling-Loops:**
  1. Erst abfragen, ob der PR überhaupt CI-Checks hat:
     ```bash
     gh pr view <nr> --json statusCheckRollup -q '.statusCheckRollup | length'
     ```
     - Resultat `0` → keine Checks konfiguriert, **direkt zu B3**.
     - Resultat `> 0` → weiter mit Schritt 2.
  2. Auf Abschluss warten mit dem **eingebauten** `gh`-Watch-Modus:
     ```bash
     gh pr checks <nr> --watch --fail-fast
     ```
     Im Hintergrund-Terminal starten und auf Fertigstellung prüfen. Hartes Maximum: **30 min**, dann abbrechen mit klarer Status-Meldung.
- **Anti-Pattern (verboten):** `while`-Schleife auf `gh pr checks` plus `sleep` — bei leerer Check-Liste (`[]`) hängt der Loop endlos. Immer Schritt 1 (Length-Check) zuerst.
- Bei Check-Fehlschlag: Ursache analysieren, fixen, neuen Commit pushen, Loop ab Schritt 1 wiederholen. Nur abbrechen bei echtem externem Blocker.

### B3. Rebase-Check vor Merge

```bash
git fetch origin main
behind=$(git rev-list --count HEAD..origin/main)
```

Wenn `behind > 0`:

```bash
git rebase origin/main
```

Konflikte eigenständig lösen (gleiche Regel wie A11). Danach:

```bash
git push --force-with-lease
```

CI wird erneut angestossen — **erneut auf grün warten** (B2-Loop), erst dann Merge.

### B4. PR mergen

- PR zum aktuellen Branch via MCP finden.
- **Squash merge** — ein sauberer Commit pro Feature/Task auf `main`.
- **Remote-Branch direkt mitlöschen**:
  - Mit `gh`: `gh pr merge <nr> --squash --delete-branch`. Lokaler Lösch-Teil darf fehlschlagen, wenn ein Worktree den Branch hält — B6 räumt lokal auf.
  - Via MCP-Merge ohne Delete-Option: nach dem Merge `git push origin --delete <branch-name>` nachschieben.

### B5. Issue schliessen

- Issue-Nummer aus Branch-Name extrahieren (`feature/<NR>-…` oder `hotfix/<NR>-…`).
- Falls nicht automatisch durch `Closes #N` geschlossen: Issue via MCP auf `closed` setzen.

### B6. Lokal aufräumen

**Worktree?** `git rev-parse --show-toplevel` vs. Haupt-Repository vergleichen.

- **Ja (Worktree)**:
  ```bash
  cd $(git worktree list --porcelain | head -1 | cut -d' ' -f2)
  git worktree remove <worktree-pfad>
  git branch -D <branch-name>
  git worktree prune
  ```
- **Nein**:
  ```bash
  git checkout main && git pull
  git branch -d <branch-name>
  ```
- Zum Schluss prüfen, dass der Remote-Branch wirklich weg ist:
  ```bash
  git push origin --delete <branch-name> 2>/dev/null || true
  git fetch --prune
  ```

### B7. Bestätigung

Knappe Schlussmeldung mit:

- Issue-Nummer (geschlossen?)
- PR-Nummer (gemergt?)
- Branch-Löschung / Worktree-Cleanup
- Allenfalls getroffene Entscheidungen, die normalerweise eine Rückfrage gewesen wären (eine Zeile pro Entscheidung).
