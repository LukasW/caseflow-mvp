# US-2 — Fall zuweisen und umverteilen

| Feld | Wert |
| --- | --- |
| ID | US-2 |
| Priorität | Hoch |
| Schätzung | M |
| Issue | [#8](https://github.com/css-ch/caseflow-mvp/issues/8) |
| Rollen | `TEAM_LEAD` |

## Beschreibung

Als **Team Lead**
möchte ich **einen Fall einer Person zuweisen und bei Bedarf umverteilen**,
damit **die Arbeit im Team klar verteilt ist und kein Fall ohne Zuständigkeit bleibt**.

## Scope

**In Scope**

- Zuweisung eines Falls an einen `CASE_MANAGER` des Teams
- Umverteilung eines bereits zugewiesenen Falls jederzeit
- Audit-Eintrag `CASE_ASSIGNED` mit Vorher/Nachher (bisherige und neue Zuständigkeit)
- Vertretung im MVP = Umverteilung durch `TEAM_LEAD`

**Out of Scope**

- Automatische Zuweisung (Round-Robin, Skill-based Routing)
- Benachrichtigung der zugewiesenen Person (E-Mail, Phase 2)
- Eigene Vertretungslogik

## Akzeptanzkriterien

- Ein `TEAM_LEAD` kann einem Fall eine zuständige Person zuweisen.
- Ein bereits zugewiesener Fall kann derselben Aktion an eine andere Person umverteilt werden.
- Jede Zuweisung und Umverteilung erzeugt einen Audit-Eintrag mit Vorher- und Nachher-Zuständigkeit.
- Ein `CASE_MANAGER` kann Fälle nicht zuweisen oder umverteilen.

## BDD-Szenarien

### Szenario 1: Fall erstmalig zuweisen (Happy Path)

- **Gegeben sei** ein Fall im Status `NEU` ohne Zuständigkeit
- **Und** ein angemeldeter `TEAM_LEAD`
- **Wenn** er den Fall einem `CASE_MANAGER` zuweist
- **Dann** trägt der Fall diese Person als Zuständigkeit
- **Und** der Audit-Trail enthält `CASE_ASSIGNED` mit leerer Vorher- und gesetzter Nachher-Zuständigkeit

### Szenario 2: Zugewiesenen Fall umverteilen (Happy Path)

- **Gegeben sei** ein Fall, der einem `CASE_MANAGER` zugewiesen ist
- **Und** ein angemeldeter `TEAM_LEAD`
- **Wenn** er den Fall einer anderen Person zuweist
- **Dann** trägt der Fall die neue Person als Zuständigkeit
- **Und** der Audit-Trail hält bisherige und neue Zuständigkeit fest

### Szenario 3: Case Manager darf nicht zuweisen (Berechtigung)

- **Gegeben sei** ein angemeldeter `CASE_MANAGER`
- **Wenn** er versucht, einen Fall zuzuweisen
- **Dann** wird der Zugriff serverseitig abgewiesen
- **Und** die Zuständigkeit des Falls bleibt unverändert

## Technische Notizen

- Betroffene Schichten: Domain (`Case.assignTo`, `AuditEntry` mit Vorher/Nachher), Application (`AssignCaseService`), Adapter in REST (`PUT /api/v1/cases/{id}/assignment`)
- Zuständigkeit als Referenz-ID aus dem IAM, kein eigenes `User`-Aggregat
- Invariante der Zuweisung wird im Aggregat durchgesetzt, nicht im Service

## Aufgaben

- [ ] Domänenmethode `assignTo` auf `Case` (liefert neue Instanz + Audit-Eintrag)
- [ ] Use-Case-Port `AssignCase` in `domain/port/in/`
- [ ] `AssignCaseService` in `application/service/`
- [ ] REST-Resource `PUT /api/v1/cases/{id}/assignment` + DTO
- [ ] Rollencheck `TEAM_LEAD` serverseitig
- [ ] Unit-Tests (Zuweisung, Umverteilung, Audit Vorher/Nachher)
- [ ] BDD-Szenarien (Backend)
- [ ] Reviewer-Agenten `hexagonal-reviewer` und `auth-security-reviewer`
