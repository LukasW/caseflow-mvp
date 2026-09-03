# US-8 — Fallübersicht: «Meine Fälle», Team-Sicht und Filter

| Feld | Wert |
| --- | --- |
| ID | US-8 |
| Priorität | Hoch |
| Schätzung | M |
| Issue | [#14](https://github.com/css-ch/caseflow-mvp/issues/14) |
| Rollen | `CASE_MANAGER`, `TEAM_LEAD` |

## Beschreibung

Als **Case Manager**
möchte ich **eine Übersicht meiner Fälle mit Filtermöglichkeiten**,
damit **ich schnell den richtigen Fall finde und den Überblick behalte**.

## Scope

**In Scope**

- Sicht «Meine Fälle» für `CASE_MANAGER` (nur eigene Zuständigkeit)
- Team-Sicht für `TEAM_LEAD` (alle Fälle des Teams)
- Filter nach Status, Zuständigkeit, Falltyp, Priorität und Fälligkeit
- Serverseitige Durchsetzung der Sichtbarkeit (kein Fremdzugriff)

**Out of Scope**

- Volltextsuche über Notizen (Phase 2)
- Dashboards und Reporting
- Spalten-Konfiguration durch den Nutzer

## Akzeptanzkriterien

- Ein `CASE_MANAGER` sieht ausschliesslich Fälle mit eigener Zuständigkeit.
- Ein `TEAM_LEAD` sieht alle Fälle des Teams.
- Die Übersicht lässt sich nach Status, Zuständigkeit, Falltyp, Priorität und Fälligkeit filtern.
- Filter lassen sich kombinieren.
- Die Sichtbarkeitsregeln werden serverseitig durchgesetzt, nicht nur im Frontend ausgeblendet.

## BDD-Szenarien

### Szenario 1: «Meine Fälle» zeigt nur eigene Fälle (Happy Path)

- **Gegeben sei** ein `CASE_MANAGER` mit zugewiesenen und nicht zugewiesenen Fällen im System
- **Wenn** er die Sicht «Meine Fälle» öffnet
- **Dann** erscheinen nur die ihm zugewiesenen Fälle

### Szenario 2: Nach Status filtern (Happy Path)

- **Gegeben sei** ein `CASE_MANAGER` mit Fällen in verschiedenen Status
- **Wenn** er nach Status `IN_BEARBEITUNG` filtert
- **Dann** erscheinen nur seine Fälle im Status `IN_BEARBEITUNG`

### Szenario 3: Team-Sicht (Berechtigung)

- **Gegeben sei** ein angemeldeter `TEAM_LEAD`
- **Wenn** er die Team-Sicht öffnet
- **Dann** sieht er alle Fälle des Teams unabhängig von der Zuständigkeit

### Szenario 4: Kein Fremdzugriff (Berechtigung)

- **Gegeben sei** ein `CASE_MANAGER`
- **Wenn** er einen ihm nicht zugewiesenen Fall direkt abruft
- **Dann** wird der Zugriff serverseitig abgewiesen

## Technische Notizen

- Betroffene Schichten: Application (`ListCasesService` mit Filter- und Sichtbarkeitslogik), Adapter in REST (`GET /api/v1/cases` mit Query-Parametern), Adapter out Persistence (Panache-Query), Frontend (Fallübersicht-Feature, Filter-UI mit Signals)
- Sichtbarkeit (`CASE_MANAGER` eigene / `TEAM_LEAD` Team) serverseitig, KVG-Gesundheitsdaten besonders schützen
- Performance-Ziel: Übersicht bis 10'000 Fälle unter 2 s (NFR)

## Aufgaben

- [ ] Use-Case-Port `ListCases` + `ListCasesService` mit Filter/Sichtbarkeit
- [ ] REST-Resource `GET /api/v1/cases` mit Query-Parametern + DTOs
- [ ] Panache-Query mit Filtern und Owner-/Team-Einschränkung
- [ ] Frontend: `case-list`-Feature, `case.service.ts`, `case.model.ts`, Route + Nav-Link
- [ ] Filter-UI mit Signals (Status, Zuständigkeit, Falltyp, Priorität, Fälligkeit)
- [ ] Unit-Tests (Filterlogik, Sichtbarkeit), Frontend-Tests (Vitest)
- [ ] BDD-Szenarien (Backend für Sichtbarkeit, `@E2E` für Übersicht/Filter)
- [ ] Reviewer-Agenten `hexagonal-reviewer`, `angular-signals-reviewer`, `auth-security-reviewer`
