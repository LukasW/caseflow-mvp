# US-4 — Frist setzen: Wiedervorlage und Fälligkeit

| Feld | Wert |
| --- | --- |
| ID | US-4 |
| Priorität | Hoch |
| Schätzung | M |
| Issue | [#10](https://github.com/css-ch/caseflow-mvp/issues/10) |
| Rollen | `CASE_MANAGER` |

## Beschreibung

Als **Case Manager**
möchte ich **zu einem Fall eine Wiedervorlage oder Fälligkeit mit Datum und Grund setzen**,
damit **gesetzliche Antwortfristen und interne Erinnerungen nicht untergehen**.

## Scope

**In Scope**

- Frist als `Deadline` mit Art `WIEDERVORLAGE` (intern) oder `FAELLIGKEIT` (gesetzlich), Termin und Grund
- Mehrere Fristen pro Fall möglich
- Fristen bearbeiten und erledigen (Status der Frist)
- Audit-Eintrag bei Setzen und Ändern einer Frist

**Out of Scope**

- E-Mail-Erinnerung (Phase 2)
- Automatische Fristberechnung aus Falltyp

## Akzeptanzkriterien

- Der zuständige `CASE_MANAGER` kann eine Frist mit Art, Termin und Grund setzen.
- Art (`WIEDERVORLAGE` / `FAELLIGKEIT`) und Termin sind Pflichtangaben.
- Ein Termin in der Vergangenheit wird abgewiesen.
- Eine gesetzte Frist kann als erledigt markiert werden.
- Setzen und Ändern einer Frist erzeugen einen Audit-Eintrag.

## BDD-Szenarien

### Szenario 1: Fälligkeit setzen (Happy Path)

- **Gegeben sei** ein Fall im Status `IN_BEARBEITUNG`, zugewiesen an den angemeldeten `CASE_MANAGER`
- **Wenn** er eine Frist der Art `FAELLIGKEIT` mit Termin und Grund setzt
- **Dann** trägt der Fall diese Frist
- **Und** der Audit-Trail enthält einen Eintrag zum Setzen der Frist

### Szenario 2: Wiedervorlage erledigen (Happy Path)

- **Gegeben sei** ein Fall mit einer offenen Wiedervorlage
- **Wenn** der zuständige `CASE_MANAGER` die Wiedervorlage als erledigt markiert
- **Dann** gilt die Wiedervorlage als erledigt und erscheint nicht mehr als offen

### Szenario 3: Termin in der Vergangenheit (Validierungsfehler)

- **Gegeben sei** ein Fall im Status `IN_BEARBEITUNG`
- **Wenn** der zuständige `CASE_MANAGER` eine Frist mit einem Termin in der Vergangenheit setzen will
- **Dann** wird die Frist nicht gesetzt
- **Und** die Rückmeldung nennt den ungültigen Termin

## Technische Notizen

- Betroffene Schichten: Domain (`Deadline` Value Object mit Art/Termin/Grund/Status, `Case.addDeadline`), Application (`SetDeadlineService`), Adapter in REST (`/api/v1/cases/{id}/deadlines`)
- `Deadline` als immutable Value Object; „Mutation" liefert neuen Fall-Zustand
- Trennung `WIEDERVORLAGE` / `FAELLIGKEIT` ist fachlich (interne vs. gesetzliche Fristen)

## Aufgaben

- [ ] `Deadline` Value Object + `DeadlineType`-Enum in `domain/model/`
- [ ] Domänenmethoden `addDeadline` und `completeDeadline` auf `Case`
- [ ] Use-Case-Port + `SetDeadlineService`
- [ ] REST-Resource `/api/v1/cases/{id}/deadlines` + DTOs
- [ ] Liquibase-Migration `deadline`-Tabelle
- [ ] Unit-Tests (Validierung Termin, Arten, Erledigen, Audit)
- [ ] BDD-Szenarien (Backend)
- [ ] Reviewer-Agent `hexagonal-reviewer`
