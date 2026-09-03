---
name: task
description: "Erstellt aus einer Beschreibung ein strukturiertes Issue (Use Case oder technischer Task) im Git-Provider und klärt Lücken interaktiv. Nur auf ausdrücklichen Aufruf (/task) ausführen, nie eigenständig aktivieren."
metadata:
  argument-hint: "<beschreibung>"
---

Erstelle ein strukturiertes Issue für die im Aufruf übergebene Beschreibung.

## 1. MCP-Server ermitteln

Lies und befolge `.github/skills/_shared/mcp-detection.md`.

## 2. Typ bestimmen

- **Use Case** (Benutzer-Feature) → Pfad A
- **Technischer Task** (Refactoring, Tests, CI/CD, Dependencies, Tech Debt) → Pfad B

Falls die Beschreibung eine bestehende `us-XX` referenziert, lies `specs/user-stories/us-XX-*.md` und verwende diesen Inhalt.

Ist der Typ nicht eindeutig → als offene Frage in Schritt 3 klären.

---

## 3. Offene Fragen klären (interaktiv, einzeln)

Prüfe die Beschreibung auf Lücken. Typische Kandidaten:

- **Typ-Zuordnung:** Pfad A vs. Pfad B unklar
- **Rolle** (Pfad A): Wer ist „Als …" — Case Manager, Team Lead, Admin, Auditor?
- **Scope:** Nur Frontend, nur Backend, beides? Welche Schicht (Domain/Adapter/…)?
- **Akzeptanzkriterien:** Fehlen messbare Erfolgsbedingungen?
- **Edge Cases:** Berechtigungen, Validierungsfehler, Fristen, Vertretung?
- **Labels / Priorität:** `enhancement` vs. `bug` vs. `maintenance` strittig?
- **Abhängigkeiten:** Blockiert durch andere US? Voraussetzungen?

**Regeln für das Nachfragen:**

- **Eine Frage pro Turn.** Nicht mehrere Fragen bündeln — erst Antwort abwarten, dann nächste Frage.
- **Immer mit Empfehlung + Pro/Contra.** Der Nutzer soll informiert entscheiden, nicht raten:

  ```markdown
  **Frage:** <konkrete Frage>

  **Empfehlung:** <Option X> — <ein-Satz-Begründung>

  **Optionen:**
  - **<Option A>**
    - Pro: <stichpunkt>
    - Contra: <stichpunkt>
  - **<Option B>**
    - Pro: <stichpunkt>
    - Contra: <stichpunkt>
  ```

- Keine Fragen zu Dingen, die du per Codebase-Lookup selbst klären kannst (z. B. existierende Dateien, aktuelle Labels im Repo).
- Nach Beantwortung aller Fragen → kurze Zusammenfassung der getroffenen Annahmen, **dann** Issue erstellen.

---

## Pfad A: Use Case

**Titel:** `US-<issue-nr>: [Prägnanter Titel]` (max. 70 Zeichen). Die Issue-Nummer = US-Nummer.

**Body:**

```markdown
## Beschreibung

Als [Rolle]
möchte ich [Ziel],
damit [Nutzen].

## Akzeptanzkriterien

- [Kriterium 1]
- [Kriterium 2]

## BDD-Szenarien

### Szenario 1: [Happy Path]
- **Gegeben sei** [Ausgangszustand]
- **Wenn** [Aktion]
- **Dann** [Ergebnis]

### Szenario 2: [Edge Case / Fehler]
- **Gegeben sei** …
- **Wenn** …
- **Dann** …

## Technische Notizen

- Betroffene Schichten: [Domain / Application / Adapter / Frontend]
- Betroffene Dateien/Module: [falls erkennbar]
- [Abhängig von US-XX]
```

**Labels:** `enhancement`.

Die lokale User-Story-Datei (`specs/user-stories/<nr>-<slug>.md`) wird von `/implement` angelegt — nicht hier.

---

## Pfad B: Technischer Task

**Titel:** `Task: [Prägnanter Titel]` (max. 70 Zeichen).

**Body:**

```markdown
## Beschreibung
[Was ist das Problem / was soll verbessert werden?]

## Motivation
[Warum nötig? Welches technische Problem löst es?]

## Umsetzung
- [Schritt 1]
- [Schritt 2]

## Betroffene Bereiche
- Schichten: [Domain / Application / Adapter / Frontend / CI-CD]
- Dateien/Module: [falls erkennbar]

## Aufgaben
- [ ] [Teilaufgabe 1]
- [ ] Tests anpassen/schreiben
- [ ] Code Review
```

**Labels:** `maintenance` oder `bug` je nach Kontext.

---

## 4. Ausgabe

- Issue-Nummer (z. B. `US-93`)
- Issue-Link

## Regeln

- BDD-Szenarien logisch ableiten (Happy Path, Edge, Fehler)
- Gherkin: Gegeben sei / Und / Wenn / Dann / Und
- Akzeptanzkriterien sind prüfbare Aussagen, keine Szenario-Wiederholung
- Sprache: Deutsch mit korrekten Umlauten
