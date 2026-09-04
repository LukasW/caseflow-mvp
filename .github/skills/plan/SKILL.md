---
name: plan
description: "Analysiert ein Issue und erstellt einen Implementierungsplan unter specs/plans/, bevor Code geschrieben wird. Nur auf ausdrücklichen Aufruf (/plan) ausführen, nie eigenständig aktivieren."
metadata:
  argument-hint: "<issue-nummer>"
---

Analysiere das im Aufruf genannte Issue (`#<nr>`) und erstelle einen Plan.

## 1. MCP-Server ermitteln

Lies und befolge `.github/skills/_shared/mcp-detection.md`.

## 2. Issue lesen

Hole Titel, Beschreibung, Labels, Kommentare via dem ermittelten MCP-Server (oder `gh issue view`).

## 3. Codebase erkunden

- Welche Domain-Modelle, Ports, Services, Adapter, Komponenten sind betroffen?
- Gibt es ähnliche bestehende Implementierungen als Vorlage?
- Welche Tests existieren bereits für die betroffenen Bereiche?

## 4. Plan als Datei speichern

Pfad: `specs/plans/us-<issue-nr>-<kurzbeschreibung>.md` (Verzeichnis bei Bedarf anlegen).

Struktur (Markdown-Tasks `- [ ]` für alle Implementierungsschritte):

```markdown
# US-<nr> — <Titel>

## Übersicht

<kurze Zusammenfassung>

## Zu erstellende Dateien

- [ ] `pfad/Datei.java` — Zweck

## Zu ändernde Dateien

- [ ] `pfad/Existing.ts` — Art der Änderung

## Architektur-Entscheide

- Layer (Domain / Application / Adapter / Frontend)
- Pattern (siehe Hexagonal/DDD in `.github/copilot-instructions.md`)

## Test-Strategie

- [ ] Unit: …
- [ ] Integration: …
- [ ] BDD-Szenarien: …

## Offene Fragen

- …
```

## 5. Backlink in die User Story

Existiert `specs/user-stories/<nr>-*.md`, ergänze dort eine Zeile in der
Kopf-Tabelle, die auf die Plan-Datei verweist (`| Plan | [<dateiname>](../plans/<dateiname>) |`).
So bleibt die Story-Datei die zentrale Einstiegsstelle mit Traceability
Issue → Plan → Tests.

## 6. Präsentieren und warten

Zeige Datei-Pfad + Zusammenfassung. **Implementiere nichts vor Bestätigung.**
Bei Korrekturen: Plan-Datei anpassen, erneut vorlegen. Erst nach „OK"/„ja"/„go": `/implement` mit dieser Issue-Nummer.
