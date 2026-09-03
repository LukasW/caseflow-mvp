# Shared Fragment: Cost & Token Discipline

Gilt für **alle** Phasen einer Implementierungs-Prompt (`/implement`, `/autoship`).
Reduziert das Kontextvolumen — der dominante Kostenfaktor pro Issue.

## Regeln

- **Jede Datei höchstens einmal lesen.** Bereits gelesene Inhalte sind im Kontext —
  kein erneutes Lesen nötig (auch nicht nach einem Edit).

- **Suche vor Volltext-Lesen.** Für Symbol-/Pattern-Suche das Such-Tool (grep/search)
  nutzen; ganze Dateien nur lesen, wenn sie auch editiert werden.

- **Subagent für das initiale Lay-of-the-Land.** Bevor 10+ `find`/`grep`/Read-Aufrufe
  sequenziell laufen, einen Subagent (Tool `agent`/`runSubagent`) mit dem Auftrag
  starten, die für das Issue relevanten Dateien zu lokalisieren und kompakt zu
  beschreiben. Die Suche läuft in isoliertem Kontext, zurück kommt nur die Liste.

- **Todo-Liste sparsam.** Maximal 5–7 Top-Level-Tasks für die ganze Implementierung
  (Setup, Implement, Test, Review, Ship). Keine Mikro-Updates pro Edit-Schritt.

- **Terminal-Output disziplinieren.** Maven-/npm-Outputs sind 50 KB+ pro Aufruf.
  - Maven: `-q` für Produktions-Builds ergänzen
  - `gh`/`git`: immer `--json … -q '…'` statt freier Text-Output
  - `find`/`ls`/`grep`: explizit `| head -N` oder `| tail -N`

- **Conditional Reviewer-Aufruf.** Vor der Reviewer-Runde **einmal**
  `git diff --name-only origin/main` ausführen und nur die Custom Agents starten,
  deren Pattern wirklich matched:
  - `hexagonal-reviewer` — nur wenn `src/main/java/**` im Diff
  - `angular-signals-reviewer` — nur wenn `src/main/webapp/src/app/**` im Diff
  - `auth-security-reviewer` — nur wenn `*Resource.java`, `oidc`,
    `application.properties` oder Security-Filter im Diff
  - `bdd-cucumber-author` — nur wenn `.feature`-Dateien neu/geändert

- **Reviewer auf günstigem Modell.** Pattern-Matching gegen Konventionen braucht kein
  Spitzenmodell — in der Agent-Datei (`.github/agents/*.agent.md`) bei Bedarf `model:`
  auf ein schnelles Modell setzen.

## Setup-Subagent (mechanische Setup-Phase)

Die mechanischen Setup-Schritte (Issue lesen + zuweisen, Branch anlegen,
User-Story-Datei) an einen Subagent delegieren. Das hält den Hauptkontext für die
eigentliche Implementierung frei; die typischen 15–25 Tool-Aufrufe (gh API, git, MCP,
File Reads) landen nicht im Hauptkontext.

Prompt-Vorlage (Auftrag an den Subagent):

> Setup für Issue #&lt;nr&gt; im caseflow-Repo:
>
> 1. Issue-Details via gh/MCP holen (`gh issue view <nr> --json title,body,labels`).
> 2. Eigenen User via `gh api user --jq .login` ermitteln und Issue zuweisen
>    (`gh issue edit <nr> --add-assignee <login>`).
> 3. Slug aus Titel (lowercase, kebab-case, max 30 Zeichen). Branch `feature/<nr>-<slug>`
>    von `origin/main` checken — falls im aktuellen Worktree bereits ein anderer Branch
>    läuft (z. B. `auto/<nr>`), diesen wiederverwenden statt einen neuen anzulegen.
> 4. In `specs/user-stories/` nach `<nr>-*.md` und thematischem Slug suchen.
>    - Fall A (Story existiert): die Datei aktualisieren — Akzeptanzkriterien,
>      BDD-Szenarien, Aufgaben-Checkliste aus dem Issue-Body ergänzen.
>    - Fall B (US-Issue ohne Story, Titel beginnt mit `US-` oder vergleichbar):
>      `specs/user-stories/<nr>-<slug>.md` neu anlegen mit Titel/ID/Priorität/
>      Schätzung, Beschreibung (Als/möchte/damit), Akzeptanzkriterien, BDD,
>      Aufgaben-Checkliste.
>    - Fall C (Bugfix/Tech-Task): keine Story-Datei.
>
> Rückgabe als knappe Liste:
> - `issueTitle`
> - `branchName` (aktiver Branch)
> - `storyFilePath` (oder null bei Fall C)
> - 3–5 wichtigste Akzeptanzkriterien
> - bemerkenswerte Constraints/Verweise aus dem Issue-Body
