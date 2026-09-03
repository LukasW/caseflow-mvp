# US-9 — Audit-Trail einsehen

| Feld | Wert |
| --- | --- |
| ID | US-9 |
| Priorität | Hoch |
| Schätzung | S |
| Issue | [#15](https://github.com/css-ch/caseflow-mvp/issues/15) |
| Rollen | `AUDITOR`, `TEAM_LEAD` |

## Beschreibung

Als **Auditor**
möchte ich **den vollständigen Audit-Trail eines Falls lesen**,
damit **jede Statusänderung und Zuweisung lückenlos nachvollziehbar ist**.

## Scope

**In Scope**

- Lesender Zugriff auf den chronologischen Audit-Trail eines Falls
- Jeder Eintrag zeigt Zeitpunkt, Akteur, Aktion und Vorher/Nachher
- Zugriff für `AUDITOR` (lesend, keine Fallbearbeitung) und `TEAM_LEAD`
- Audit-Trail ist append-only und unveränderbar

**Out of Scope**

- Bearbeiten oder Löschen von Audit-Einträgen
- Reporting/Export über die Einzelfall-Sicht hinaus
- Volltextsuche im Audit-Trail

## Akzeptanzkriterien

- Ein `AUDITOR` kann den vollständigen Audit-Trail eines Falls chronologisch lesen.
- Jeder Eintrag enthält Zeitpunkt, Akteur, Aktion und – wo zutreffend – Vorher- und Nachher-Wert.
- Der Audit-Trail ist append-only; kein Nutzer kann Einträge ändern oder löschen.
- Ein `AUDITOR` hat keinen Zugriff auf die Fallbearbeitung.
- Rollen ohne Audit-Recht (z. B. reiner `CASE_MANAGER` bei fremden Fällen) erhalten keinen Zugriff.

## BDD-Szenarien

### Szenario 1: Audit-Trail lesen (Happy Path)

- **Gegeben sei** ein Fall, der erfasst, zugewiesen und dessen Status geändert wurde
- **Und** ein angemeldeter `AUDITOR`
- **Wenn** er den Audit-Trail des Falls öffnet
- **Dann** sieht er die Einträge `CASE_CREATED`, `CASE_ASSIGNED` und `CASE_STATUS_CHANGED` in chronologischer Reihenfolge
- **Und** jeder Eintrag zeigt Zeitpunkt, Akteur und Vorher/Nachher

### Szenario 2: Audit-Trail ist unveränderbar

- **Gegeben sei** ein Fall mit vorhandenen Audit-Einträgen
- **Wenn** ein Nutzer versucht, einen Audit-Eintrag zu ändern oder zu löschen
- **Dann** wird die Operation abgewiesen
- **Und** der Audit-Trail bleibt unverändert

### Szenario 3: Auditor darf nicht bearbeiten (Berechtigung)

- **Gegeben sei** ein angemeldeter `AUDITOR`
- **Wenn** er einen Fall bearbeiten will
- **Dann** wird der Zugriff serverseitig abgewiesen

## Technische Notizen

- Betroffene Schichten: Domain (`AuditEntry` als Teil des `Case`-Aggregats, append-only), Application (`ReadAuditTrailService`), Adapter in REST (`GET /api/v1/cases/{id}/audit`, lesend für `AUDITOR`/`TEAM_LEAD`)
- Audit-Trail entsteht als Nebenergebnis der Domänenmethoden (US-1 bis US-7), diese Story macht ihn lesbar
- Unveränderbarkeit auch auf Persistenz-Ebene absichern (keine Update-/Delete-Pfade)

## Aufgaben

- [ ] Use-Case-Port `ReadAuditTrail` + `ReadAuditTrailService`
- [ ] REST-Resource `GET /api/v1/cases/{id}/audit` + DTO
- [ ] Rollencheck `AUDITOR`/`TEAM_LEAD` serverseitig, kein Bearbeitungsrecht für `AUDITOR`
- [ ] Persistenz append-only sicherstellen (keine Update/Delete auf Audit)
- [ ] Frontend: Audit-Trail-Ansicht am Fall (lesend)
- [ ] Unit-/Integrationstests (Chronologie, Unveränderbarkeit, Berechtigung)
- [ ] BDD-Szenarien (Backend, `@E2E` für Ansicht)
- [ ] Reviewer-Agenten `hexagonal-reviewer` und `auth-security-reviewer`
