---
name: fast
description: "Schnellstarter: erstellt Issue, plant und implementiert in einem Durchlauf (kombiniert die Skills task, plan und implement). Nur auf ausdrücklichen Aufruf (/fast) ausführen, nie eigenständig aktivieren."
metadata:
  argument-hint: "<beschreibung>"
---

Führe die folgenden Schritte nacheinander aus.

**Eingabe:** die im Aufruf übergebene Beschreibung.

## Schritt 0: Duplikat-Prüfung

1. Codebase durchsuchen auf Hinweise, dass das Feature bereits existiert
2. Offene und kürzlich geschlossene Issues via MCP (oder `gh issue list`) prüfen

**Falls Duplikat:** Klare Meldung (Datei/Komponente oder Issue-Link), sofort abbrechen. **Kein neues Issue erstellen.**

## Schritt 1: Issue erstellen

Führe die Anweisungen aus Skill `task` mit obiger Beschreibung aus. Merke die Issue-Nummer.

Bei echten Unklarheiten (mehrdeutige Anforderungen, unklar ob US oder Task): nachfragen, auf Antwort warten.

## Schritt 2: Plan erstellen

Führe die Anweisungen aus Skill `plan` mit der Issue-Nummer aus.

## Schritt 3: Plan-Bestätigung — nur wenn nötig

Normalerweise überspringt `/fast` die Plan-Bestätigung. **Ausnahme — lege den Plan dem User vor und warte auf Bestätigung**, wenn mindestens eine dieser Heuristiken zutrifft:

- Use-Case-Issue mit **mehr als 3 Akzeptanzkriterien**
- Plan berührt **mehr als eine Schicht** (z. B. Domain + Adapter + Frontend)
- Plan enthält **offene Fragen** oder mehrere valide Lösungsansätze
- Migration (Liquibase), Auth/Security, Audit-Trail-Änderung oder Breaking Changes berührt
- Plan-Datei hat **mehr als 30 Zeilen** nach dem ersten Entwurf

Sonst: direkt weiter zu Schritt 4.

## Schritt 4: Implementieren

Führe die Anweisungen aus Skill `implement` mit der Issue-Nummer aus.

## Regeln

- Automatischer Durchlauf — bei echten Unklarheiten interaktiv nachfragen
- Bei zutreffender Heuristik (Schritt 3): Plan-Bestätigung einholen
- Fehler in einem Schritt: melden und abbrechen, nicht weitermachen
- Duplikat erkannt = sofortiger Abbruch
