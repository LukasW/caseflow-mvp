# US-1 — Fall erfassen

| Feld | Wert |
| --- | --- |
| ID | US-1 |
| Priorität | Hoch |
| Schätzung | M |
| Issue | [#7](https://github.com/css-ch/caseflow-mvp/issues/7) |
| Plan | [us-7-fall-erfassen.md](../plans/us-7-fall-erfassen.md) |
| Feature | [fall-erfassen.feature](../../src/test/resources/features/case/fall-erfassen.feature) |
| Rollen | `CASE_MANAGER` |

## Beschreibung

Als **Case Manager**
möchte ich **einen neuen Fall mit seinen Grunddaten erfassen**,
damit **ein eingehendes Anliegen strukturiert im Tool statt in E-Mail oder Excel liegt**.

## Scope

**In Scope**

- Erfassen mit Grunddaten: Fallreferenz (Kundenreferenz als ID), Falltyp, Priorität, Quelle
- Neuer Fall startet im Status `NEU` ohne Zuständigkeit
- Pflichtfeld-Validierung serverseitig (fail fast an der REST-Grenze)
- Erster Audit-Eintrag `CASE_CREATED` beim Anlegen

**Out of Scope**

- Validierung der Kundenreferenz gegen das Kernsystem (Referenz bleibt reine ID)
- Dokumenten-Upload
- Automatische Zuweisung

## Akzeptanzkriterien

- Ein `CASE_MANAGER` kann einen Fall mit Falltyp, Priorität, Quelle und Kundenreferenz anlegen.
- Ein angelegter Fall erhält eine eindeutige Fallnummer und den Status `NEU`.
- Fehlt ein Pflichtfeld, wird der Fall nicht angelegt und die fehlenden Felder werden benannt.
- Das Anlegen erzeugt einen unveränderbaren Audit-Eintrag mit Akteur und Zeitpunkt.
- Andere Rollen ohne Erfassungsrecht können keinen Fall anlegen.

## BDD-Szenarien

### Szenario 1: Fall mit vollständigen Grunddaten erfassen (Happy Path)

- **Gegeben sei** ein angemeldeter `CASE_MANAGER`
- **Wenn** er einen Fall mit Falltyp, Priorität, Quelle und Kundenreferenz erfasst
- **Dann** wird der Fall mit einer eindeutigen Fallnummer im Status `NEU` gespeichert
- **Und** der Audit-Trail enthält einen Eintrag `CASE_CREATED` mit seinem Namen und Zeitpunkt

### Szenario 2: Pflichtfeld fehlt (Validierungsfehler)

- **Gegeben sei** ein angemeldeter `CASE_MANAGER`
- **Wenn** er einen Fall ohne Falltyp erfassen will
- **Dann** wird der Fall nicht angelegt
- **Und** die Rückmeldung nennt den Falltyp als fehlendes Pflichtfeld

### Szenario 3: Rolle ohne Erfassungsrecht (Berechtigung)

- **Gegeben sei** ein angemeldeter `AUDITOR`
- **Wenn** er einen Fall erfassen will
- **Dann** wird der Zugriff serverseitig abgewiesen
- **Und** es wird kein Fall angelegt

## Technische Notizen

- Betroffene Schichten: Domain (`Case` Aggregate Root, Value Objects Falltyp/Priorität/Quelle), Application (`CreateCaseService`), Adapter in REST (`POST /api/v1/cases`, DTOs), Adapter out Persistence (`CaseEntity`, `CaseRepository`), Liquibase-Migration
- Statusmodell: neuer Fall im Status `NEU`
- Audit-Trail ist Teil der Domäne (`AuditEntry`), kein Logging-Nebeneffekt

## Aufgaben

- [x] `Case` Aggregate Root + Value Objects (`CaseType`, `Priority`, `Source`, `CaseReference`) in `domain/model/`
- [x] `AuditEntry` Value Object in `domain/model/`
- [x] Use-Case-Port `CreateCase` in `domain/port/in/`
- [x] Repository-Port `CaseRepository` in `domain/port/out/`
- [x] `CreateCaseService` in `application/service/`
- [x] `CaseEntity` + Panache-`CaseRepository`-Adapter in `adapter/out/persistence/`
- [x] REST-Resource `POST /api/v1/cases` + DTOs in `adapter/in/rest/`
- [x] Liquibase-Migration `case`-Tabelle + Include in `db/changeLog.xml`
- [x] Unit-Tests (Domain-Invarianten, `CreateCaseService` mit gemocktem Port)
- [x] BDD-Szenarien (Backend, REST-assured)
- [x] Reviewer-Agenten `hexagonal-reviewer` und `auth-security-reviewer`
