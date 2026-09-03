# ADR-01: Hexagonal Architecture und DDD Tactical Design

| | |
|---|---|
| Status | Akzeptiert |
| Datum | 2026-09-03 |
| Entscheider | Software-Architektur CaseFlow |
| Bezug arc42 | Kapitel 4, 5, 8 |

## Kontext

CaseFlow soll schnell als MVP starten, aber über Jahre mit wachsender Fachlogik
(Fall-Lebenszyklus, Fristenlogik, Vertretungsregeln, Audit) wartbar bleiben.
Die Geschäftslogik muss testbar und gegen Framework-Wechsel robust sein.

## Entscheidung

Das System folgt der **Hexagonalen Architektur (Ports & Adapters)** mit einem
framework-freien Domänenkern. Innerhalb des Kerns strukturiert **DDD Tactical
Design** die Fachlogik: Aggregate, Entities, Value Objects, Domain Services,
Domain Events, Factories. Driving Ports (`port/in/`) definieren Use Cases,
Driven Ports (`port/out/`) definieren, was die Domäne nach aussen benötigt.

## Begründung

- Der Domänenkern bleibt **ohne Framework-Abhängigkeiten** und damit als reine
  Java-Logik isoliert testbar.
- Klare Modulgrenzen; Abhängigkeiten zeigen ausnahmslos nach innen.
- Konsistenz mit den bestehenden CSS-Demo-Projekten (gleiche Konventionen, gleiche Reviewer-Agenten).

## Konsequenzen

- `domain/` enthält keine Quarkus-, JPA- oder Jackson-Imports — ArchUnit erzwingt das.
- JPA-Entities leben getrennt in `adapter/out/persistence/`; Mapping zwischen
  Domänenmodell und Persistenzmodell ist erforderlich (bewusster Mehraufwand).
- Disziplin nötig: Adapter dürfen den Kern nie umgehen.

## Verworfene Alternativen

- **Klassische Schichtenarchitektur** — verwischt die Grenzen, koppelt die
  Domäne an das Persistenz-Framework.
- **Transaction Script** — skaliert nicht mit der fachlichen Komplexität von
  Fristen, Vertretung und Audit.
