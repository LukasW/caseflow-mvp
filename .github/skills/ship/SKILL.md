---
name: ship
description: "Schliesst die aktuelle Arbeit ab: CI abwarten, PR squash-mergen, Issue schliessen, Branch und Worktree aufräumen. Nur auf ausdrücklichen Aufruf (/ship) ausführen, nie eigenständig aktivieren."
compatibility: "Requires git, gh (GitHub CLI) oder einen Git-Provider-MCP"
---

Schliesse die aktuelle Arbeit ab (nach `/implement`).

## 1. MCP-Server ermitteln

Lies `.github/skills/_shared/mcp-detection.md`.

## 2. Branch & Push

- Branch-Namen merken: `git branch --show-current`
- `git status` — bei uncommitteten Änderungen: abbrechen und User fragen
- `git push` falls noch nicht geschehen

## 3. DoD & CI

- DoD erfüllt? Abgleichen mit `.github/skills/_shared/dod-checklist.md`
- **CI-Wait — robust, ohne selbstgebaute Polling-Loops:**
  1. Erst abfragen, ob der PR überhaupt CI-Checks hat:
     ```bash
     gh pr view <nr> --json statusCheckRollup -q '.statusCheckRollup | length'
     ```
     - Resultat `0` → keine Checks konfiguriert, **direkt zu Schritt 4**.
     - Resultat `> 0` → weiter mit Schritt 2.
  2. Auf Abschluss warten mit dem **eingebauten** `gh`-Watch-Modus, nicht mit selbstgebauten `while true; do gh pr checks ...; sleep; done`-Loops:
     ```bash
     gh pr checks <nr> --watch --fail-fast
     ```
     Im Hintergrund-Terminal starten und auf Fertigstellung prüfen. Hartes Maximum: **30 min**, dann abbrechen mit klarer Status-Meldung.
- **Anti-Pattern (verboten):** `while`-Schleife auf `gh pr checks` plus `sleep` — wenn die Check-Liste leer (`[]`) ist, hängt der Loop endlos. Immer Schritt 1 (Length-Check) machen, **bevor** auf Checks gewartet wird.
- Bei Fehlschlag: abbrechen, User informieren, nicht mergen

## 4. PR mergen

- PR zum aktuellen Branch via MCP finden
- **Squash merge** — ein sauberer Commit pro Feature/Task auf `main`
- **Remote-Branch direkt mitlöschen**, damit kein Branch-Friedhof entsteht:
  - Mit `gh`: `gh pr merge <nr> --squash --delete-branch`. Der lokale
    Lösch-Teil darf fehlschlagen, wenn ein Worktree den Branch hält —
    Meldung ignorieren, Schritt 6 räumt lokal auf.
  - Via MCP-Merge ohne Delete-Option: nach dem Merge
    `git push origin --delete <branch-name>` nachschieben.

## 5. Issue schliessen

- Issue-Nummer aus Branch-Name extrahieren (`feature/<NR>-…` oder `hotfix/<NR>-…`)
- Falls nicht automatisch durch `Closes #N` geschlossen: Issue via MCP auf `closed` setzen

## 6. Lokal aufräumen

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
- Zum Schluss prüfen, dass der Remote-Branch wirklich weg ist (falls
  Schritt 4 ihn nicht löschen konnte):
  ```bash
  git push origin --delete <branch-name> 2>/dev/null || true
  git fetch --prune
  ```

## 7. Bestätigung

Gib aus: Issue-Nummer, PR-Nummer, Merge-Status, Branch-Löschung (lokal **und** remote).
