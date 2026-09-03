# US-3 — Fall bearbeiten: Status und Notizen

| Feld | Wert |
| --- | --- |
| ID | US-3 |
| Priorität | Hoch |
| Schätzung | M |
| Issue | [#9](https://github.com/css-ch/caseflow-mvp/issues/9) |
| Rollen | `CASE_MANAGER` |

## Beschreibung

Als **Case Manager**
möchte ich **den Status eines Falls ändern und Notizen hinzufügen**,
damit **der Bearbeitungsstand jederzeit aktuell und nachvollziehbar ist**.

## Scope

**In Scope**

- Statuswechsel entlang des erlaubten Statusmodells: `NEU` → `IN_BEARBEITUNG` → `WARTEND` ↔ `IN_BEARBEITUNG`
- Notiz zu einem Fall hinzufügen (Fallinhalt, nicht Audit)
- Audit-Eintrag `CASE_STATUS_CHANGED` mit Vorher/Nachher bei jedem Statuswechsel
- Nur die zuständige Person (bzw. `TEAM_LEAD`) bearbeitet den Fall

**Out of Scope**

- Abschluss und Wiedereröffnung (US-6, US-7)
- Volltextsuche über Notizen
- Bearbeitung abgeschlossener Fälle

## Akzeptanzkriterien

- Der zuständige `CASE_MANAGER` kann den Status entlang der erlaubten Übergänge wechseln.
- Ein unerlaubter Statusübergang wird abgewiesen und der Status bleibt unverändert.
- Eine Notiz wird mit Autor und Zeitpunkt am Fall gespeichert.
- Jeder Statuswechsel erzeugt einen Audit-Eintrag mit Vorher- und Nachher-Status.
- Nur die zuständige Person oder ein `TEAM_LEAD` kann den Fall bearbeiten.

## BDD-Szenarien

### Szenario 1: Fall in Bearbeitung nehmen (Happy Path)

- **Gegeben sei** ein Fall im Status `NEU`, zugewiesen an den angemeldeten `CASE_MANAGER`
- **Wenn** er den Status auf `IN_BEARBEITUNG` setzt
- **Dann** trägt der Fall den Status `IN_BEARBEITUNG`
- **Und** der Audit-Trail enthält `CASE_STATUS_CHANGED` von `NEU` auf `IN_BEARBEITUNG`

### Szenario 2: Notiz hinzufügen (Happy Path)

- **Gegeben sei** ein Fall im Status `IN_BEARBEITUNG`, zugewiesen an den angemeldeten `CASE_MANAGER`
- **Wenn** er eine Notiz erfasst
- **Dann** ist die Notiz mit Autor und Zeitpunkt am Fall sichtbar

### Szenario 3: Unerlaubter Statusübergang (Fehler)

- **Gegeben sei** ein Fall im Status `NEU`
- **Wenn** der zuständige `CASE_MANAGER` den Status direkt auf `WARTEND` setzen will
- **Dann** wird der Übergang abgewiesen
- **Und** der Fall bleibt im Status `NEU`

### Szenario 4: Fremder Fall (Berechtigung)

- **Gegeben sei** ein Fall, der einer anderen Person zugewiesen ist
- **Wenn** ein nicht zuständiger `CASE_MANAGER` den Status ändern will
- **Dann** wird der Zugriff serverseitig abgewiesen

## Technische Notizen

- Betroffene Schichten: Domain (`Case.changeStatus`, `Case.addNote`, Statusübergangs-Invarianten), Application (`UpdateCaseService`), Adapter in REST (`PATCH /api/v1/cases/{id}`, `POST /api/v1/cases/{id}/notes`)
- Statusübergänge als Invariante im Aggregat; unerlaubte Übergänge werfen fail fast
- Owner-Check (`CASE_MANAGER` sieht/bearbeitet nur eigene Fälle) serverseitig

## Aufgaben

- [ ] `CaseStatus`-Enum + Übergangsregeln im `Case`-Aggregat
- [ ] Domänenmethoden `changeStatus` und `addNote`
- [ ] `Note` Value Object in `domain/model/`
- [ ] Use-Case-Port + `UpdateCaseService`
- [ ] REST-Resourcen für Statuswechsel und Notiz + DTOs
- [ ] Owner-/Rollencheck serverseitig
- [ ] Liquibase-Migration `note`-Tabelle
- [ ] Unit-Tests (Übergangsmatrix, Notiz, Audit)
- [ ] BDD-Szenarien (Backend)
- [ ] Reviewer-Agenten `hexagonal-reviewer` und `auth-security-reviewer`
