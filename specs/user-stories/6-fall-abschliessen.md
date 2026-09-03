# US-6 — Fall abschliessen

| Feld | Wert |
| --- | --- |
| ID | US-6 |
| Priorität | Hoch |
| Schätzung | S |
| Issue | [#12](https://github.com/css-ch/caseflow-mvp/issues/12) |
| Rollen | `CASE_MANAGER` |

## Beschreibung

Als **Case Manager**
möchte ich **einen Fall mit einem pflichtigen Abschlussgrund abschliessen**,
damit **klar dokumentiert ist, warum ein Fall erledigt ist, und er nicht mehr verändert wird**.

## Scope

**In Scope**

- Abschluss eines Falls aus `IN_BEARBEITUNG` oder `WARTEND` in den Status `ABGESCHLOSSEN`
- Abschlussgrund ist Pflichtfeld
- Abgeschlossener Fall ist read-only (keine Statuswechsel, Notizen, Fristen mehr)
- Audit-Eintrag `CASE_CLOSED` mit Grund

**Out of Scope**

- Wiedereröffnung (US-7)
- Automatischer Abschluss

## Akzeptanzkriterien

- Der zuständige `CASE_MANAGER` kann einen Fall unter Angabe eines Abschlussgrunds abschliessen.
- Ohne Abschlussgrund wird der Fall nicht abgeschlossen.
- Ein abgeschlossener Fall lässt keine weiteren Änderungen zu (read-only).
- Der Abschluss erzeugt einen Audit-Eintrag `CASE_CLOSED` mit Grund und Akteur.

## BDD-Szenarien

### Szenario 1: Fall abschliessen (Happy Path)

- **Gegeben sei** ein Fall im Status `IN_BEARBEITUNG`, zugewiesen an den angemeldeten `CASE_MANAGER`
- **Wenn** er den Fall mit einem Abschlussgrund abschliesst
- **Dann** trägt der Fall den Status `ABGESCHLOSSEN`
- **Und** der Audit-Trail enthält `CASE_CLOSED` mit dem Grund

### Szenario 2: Abschluss ohne Grund (Validierungsfehler)

- **Gegeben sei** ein Fall im Status `IN_BEARBEITUNG`
- **Wenn** der zuständige `CASE_MANAGER` den Fall ohne Abschlussgrund abschliessen will
- **Dann** wird der Fall nicht abgeschlossen
- **Und** die Rückmeldung nennt den fehlenden Abschlussgrund

### Szenario 3: Abgeschlossener Fall ist read-only (Fehler)

- **Gegeben sei** ein Fall im Status `ABGESCHLOSSEN`
- **Wenn** jemand seinen Status ändern oder eine Notiz hinzufügen will
- **Dann** wird die Änderung abgewiesen
- **Und** der Fall bleibt unverändert

## Technische Notizen

- Betroffene Schichten: Domain (`Case.close`, Read-only-Invariante bei `ABGESCHLOSSEN`), Application (`CloseCaseService`), Adapter in REST (`POST /api/v1/cases/{id}/closure`)
- Read-only wird als Invariante im Aggregat durchgesetzt, nicht nur im Frontend
- Abschlussgrund als Value Object mit Pflichtvalidierung

## Aufgaben

- [ ] Domänenmethode `close` + Read-only-Invariante im `Case`-Aggregat
- [ ] `ClosureReason` Value Object
- [ ] Use-Case-Port + `CloseCaseService`
- [ ] REST-Resource `POST /api/v1/cases/{id}/closure` + DTO
- [ ] Liquibase-Migration Abschlussgrund-Feld
- [ ] Unit-Tests (Abschluss, Pflichtgrund, Read-only nach Abschluss)
- [ ] BDD-Szenarien (Backend)
- [ ] Reviewer-Agent `hexagonal-reviewer`
