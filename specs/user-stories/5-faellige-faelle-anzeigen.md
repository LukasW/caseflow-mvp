# US-5 — Fällige und überfällige Fälle anzeigen

| Feld | Wert |
| --- | --- |
| ID | US-5 |
| Priorität | Hoch |
| Schätzung | S |
| Issue | [#11](https://github.com/css-ch/caseflow-mvp/issues/11) |
| Rollen | `CASE_MANAGER`, `TEAM_LEAD` |

## Beschreibung

Als **Case Manager**
möchte ich **fällige und überfällige Fälle in meiner Übersicht aktiv markiert sehen**,
damit **ich Fristen einhalte, ohne selbst nachrechnen zu müssen**.

## Scope

**In Scope**

- Fälle mit heute oder früher fälliger, noch offener Frist werden als „fällig" bzw. „überfällig" markiert
- Markierung in der Fallübersicht (In-App, keine Benachrichtigung)
- Sortierung/Hervorhebung überfälliger Fälle
- `TEAM_LEAD` sieht die Fälligkeiten des Teams

**Out of Scope**

- E-Mail- oder Push-Benachrichtigung (Phase 2)
- Kalender-Export

## Akzeptanzkriterien

- Ein Fall mit einer offenen Frist, deren Termin heute ist, wird als „fällig" markiert.
- Ein Fall mit einer offenen Frist, deren Termin in der Vergangenheit liegt, wird als „überfällig" markiert.
- Erledigte Fristen führen nicht zu einer Markierung.
- Ein `CASE_MANAGER` sieht nur die Fälligkeiten seiner eigenen Fälle, ein `TEAM_LEAD` die des Teams.

## BDD-Szenarien

### Szenario 1: Überfälliger Fall wird markiert (Happy Path)

- **Gegeben sei** ein mir zugewiesener Fall mit einer offenen Frist, deren Termin gestern war
- **Wenn** ich meine Fallübersicht öffne
- **Dann** ist der Fall als „überfällig" markiert

### Szenario 2: Erledigte Frist erzeugt keine Markierung

- **Gegeben sei** ein mir zugewiesener Fall, dessen einzige Frist erledigt ist
- **Wenn** ich meine Fallübersicht öffne
- **Dann** ist der Fall weder als „fällig" noch als „überfällig" markiert

### Szenario 3: Team-Sicht der Fälligkeiten (Berechtigung)

- **Gegeben sei** ein angemeldeter `TEAM_LEAD`
- **Wenn** er die Team-Übersicht öffnet
- **Dann** sieht er die fälligen und überfälligen Fälle aller Teammitglieder

## Technische Notizen

- Betroffene Schichten: Domain (Fälligkeitsermittlung aus `Deadline`-Status und Termin), Application (`ListDueCasesService` bzw. Erweiterung der Übersicht), Adapter in REST (Query-Parameter/Projektion in `/api/v1/cases`), Frontend (Markierung in der Fallübersicht)
- Fälligkeit ist abgeleiteter Zustand, keine gespeicherte Flag-Spalte
- Owner-/Team-Filter serverseitig

## Aufgaben

- [ ] Domänenlogik „fällig/überfällig" aus offenen `Deadline`s
- [ ] Erweiterung der Übersichts-Query (Filter/Projektion Fälligkeit)
- [ ] REST-Projektion mit Fälligkeitsstatus
- [ ] Frontend: Markierung „fällig"/„überfällig" in der Fallübersicht
- [ ] Unit-Tests (Fälligkeitsermittlung, erledigte Fristen)
- [ ] BDD-Szenarien (Backend für Ermittlung, `@E2E` für Anzeige)
- [ ] Reviewer-Agenten `hexagonal-reviewer` und `angular-signals-reviewer`
