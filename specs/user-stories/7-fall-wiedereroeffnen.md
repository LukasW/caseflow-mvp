# US-7 — Fall wiedereröffnen

| Feld | Wert |
| --- | --- |
| ID | US-7 |
| Priorität | Mittel |
| Schätzung | S |
| Issue | [#13](https://github.com/css-ch/caseflow-mvp/issues/13) |
| Rollen | `ADMIN` |

## Beschreibung

Als **Admin**
möchte ich **einen abgeschlossenen Fall mit Begründung wiedereröffnen**,
damit **ein zu früh oder irrtümlich abgeschlossener Fall weiterbearbeitet werden kann**.

## Scope

**In Scope**

- Wiedereröffnung eines Falls aus `ABGESCHLOSSEN` zurück nach `IN_BEARBEITUNG`
- Begründung ist Pflichtfeld
- Nur `ADMIN` darf wiedereröffnen
- Audit-Eintrag `CASE_REOPENED` mit Begründung

**Out of Scope**

- Wiedereröffnung durch andere Rollen
- Automatische Wiedereröffnung

## Akzeptanzkriterien

- Ein `ADMIN` kann einen abgeschlossenen Fall unter Angabe einer Begründung wiedereröffnen.
- Der Fall wechselt dabei von `ABGESCHLOSSEN` nach `IN_BEARBEITUNG` und ist wieder bearbeitbar.
- Ohne Begründung wird nicht wiedereröffnet.
- Keine andere Rolle als `ADMIN` kann wiedereröffnen.
- Die Wiedereröffnung erzeugt einen Audit-Eintrag `CASE_REOPENED` mit Begründung und Akteur.

## BDD-Szenarien

### Szenario 1: Fall wiedereröffnen (Happy Path)

- **Gegeben sei** ein Fall im Status `ABGESCHLOSSEN`
- **Und** ein angemeldeter `ADMIN`
- **Wenn** er den Fall mit einer Begründung wiedereröffnet
- **Dann** trägt der Fall den Status `IN_BEARBEITUNG`
- **Und** der Audit-Trail enthält `CASE_REOPENED` mit der Begründung

### Szenario 2: Wiedereröffnung ohne Begründung (Validierungsfehler)

- **Gegeben sei** ein Fall im Status `ABGESCHLOSSEN`
- **Wenn** ein `ADMIN` ihn ohne Begründung wiedereröffnen will
- **Dann** wird der Fall nicht wiedereröffnet
- **Und** die Rückmeldung nennt die fehlende Begründung

### Szenario 3: Case Manager darf nicht wiedereröffnen (Berechtigung)

- **Gegeben sei** ein Fall im Status `ABGESCHLOSSEN`
- **Wenn** ein `CASE_MANAGER` ihn wiedereröffnen will
- **Dann** wird der Zugriff serverseitig abgewiesen
- **Und** der Fall bleibt `ABGESCHLOSSEN`

## Technische Notizen

- Betroffene Schichten: Domain (`Case.reopen`), Application (`ReopenCaseService`), Adapter in REST (`POST /api/v1/cases/{id}/reopening`, Rollencheck `ADMIN`)
- Wiedereröffnung ist der einzige Ausweg aus dem read-only Zustand nach Abschluss
- Begründung als Value Object mit Pflichtvalidierung

## Aufgaben

- [ ] Domänenmethode `reopen` auf `Case` (nur aus `ABGESCHLOSSEN`)
- [ ] Use-Case-Port + `ReopenCaseService`
- [ ] REST-Resource `POST /api/v1/cases/{id}/reopening` + DTO
- [ ] Rollencheck `ADMIN` serverseitig
- [ ] Unit-Tests (Wiedereröffnung, Pflichtbegründung, Berechtigung)
- [ ] BDD-Szenarien (Backend)
- [ ] Reviewer-Agenten `hexagonal-reviewer` und `auth-security-reviewer`
