---
name: adr
description: "Erstellt einen Architecture Decision Record unter specs/adr/, aktualisiert Index und mkdocs-Navigation und klärt Lücken interaktiv. Nur auf ausdrücklichen Aufruf (/adr) ausführen, nie eigenständig aktivieren."
metadata:
  argument-hint: "<titel oder entscheidungsbeschreibung>"
---

Erstelle einen Architecture Decision Record (ADR) für die im Aufruf übergebene
Entscheidung. Grundlage sind die Konventionen in `specs/adr/README.md`.

## 1. Bestehende ADRs sichten

- `specs/adr/README.md` lesen (Index-Tabelle, Konventionen).
- Prüfen, ob die Entscheidung eine bestehende ADR **ablöst** (dann Status der alten
  ADR auf `Abgelöst durch ADR-XX` setzen) oder eine neue ist.
- Nächste freie Nummer bestimmen: höchste `adr-NN-*.md` + 1. IDs werden **nie**
  wiederverwendet.

## 2. Offene Fragen klären (interaktiv, einzeln)

Prüfe die Beschreibung auf Lücken. Typische Kandidaten:

- **Kontext:** Welches Problem/welcher Kräftekonflikt erzwingt die Entscheidung?
- **Entscheidung:** Was genau wird festgelegt? Eindeutig formuliert?
- **Alternativen:** Welche Optionen wurden erwogen und warum verworfen?
- **Konsequenzen:** Positive wie negative Folgen, bewusst in Kauf genommener Mehraufwand?
- **Status:** `Vorgeschlagen` oder direkt `Akzeptiert`? Löst er eine ADR ab?
- **arc42-Bezug:** Welche Kapitel betrifft der Entscheid?

**Regeln für das Nachfragen:**

- **Eine Frage pro Turn.** Erst Antwort abwarten, dann nächste Frage.
- **Immer mit Empfehlung + kurzer Begründung**, damit der Nutzer informiert entscheidet.
- Keine Fragen zu Dingen, die per Codebase-Lookup selbst klärbar sind (bestehende
  ADRs, arc42-Struktur, aktuelle Architekturregeln).
- Nach allen Antworten: kurze Zusammenfassung der Annahmen, **dann** ADR anlegen.

## 3. ADR-Datei anlegen

Pfad: `specs/adr/adr-<NN>-<slug>.md` (NN zweistellig, slug kebab-case, ASCII).

Struktur (exakt an den Bestand angleichen — Format: Kontext → Entscheidung →
Begründung → Konsequenzen → Verworfene Alternativen):

```markdown
# ADR-<NN>: <Titel>

| | |
|---|---|
| Status | <Vorgeschlagen \| Akzeptiert> |
| Datum | <YYYY-MM-DD> |
| Entscheider | <Rolle/Gremium> |
| Bezug arc42 | <Kapitel …> |

## Kontext

<Problem, Kräftekonflikt, Randbedingungen.>

## Entscheidung

<Was wird festgelegt — eindeutig, aktiv formuliert.>

## Begründung

- <Grund 1>
- <Grund 2>

## Konsequenzen

- <positive und negative Folgen, bewusster Mehraufwand>

## Verworfene Alternativen

- **<Alternative>** — <warum verworfen>
```

## 4. Index und Navigation aktualisieren

- **`specs/adr/README.md`**: neue Zeile in der Index-Tabelle ergänzen
  (`| [ADR-<NN>](adr-<NN>-<slug>.md) | <Titel> | <Status> |`). Bei Ablösung den
  Status der abgelösten ADR mitpflegen.
- **`mkdocs.yml`**: unter `nav:` → `ADRs:` einen Eintrag
  `- ADR-<NN> <Kurztitel>: adr/adr-<NN>-<slug>.md` anhängen.
- Wird eine ADR abgelöst: deren Statuszeile in der ADR-Datei aktualisieren.

## 5. Ausgabe

- ADR-ID und Datei-Pfad
- Kurze Zusammenfassung der Entscheidung
- Hinweis, welche ADR ggf. abgelöst wurde

## Regeln

- Sprache: Deutsch mit korrekten Umlauten, Schweizer Hochdeutsch ohne `ß`.
- ASCII-Transliteration nur im Dateinamen-Slug, nie im Fliesstext.
- Ein Entscheid = ein Record. Keine Sammel-ADRs.
- ADR nicht inline in arc42 oder Spezifikationen ablegen — immer als eigener Record.
