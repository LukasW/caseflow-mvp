<!-- Kurzer fachlicher Titel + Issue-Referenz im PR-Titel: `<typ>(<scope>): <Beschreibung> (#<nr>)` -->

## Zusammenfassung

<!-- Was ändert sich fachlich? Warum? Closes #<nr>. -->

## Checkliste

- [ ] Akzeptanzkriterien aus dem verlinkten Issue umgesetzt
- [ ] Tests für geändertes Verhalten ergänzt (Unit + ggf. `@QuarkusTest` /
      Frontend-Vitest / Cucumber)
- [ ] Hexagonal-Schichten respektiert (`domain/` ohne Framework-Imports,
      Adapter rufen Ports auf, nicht umgekehrt)
- [ ] Statusänderungen am Aggregat erzeugen Audit-Einträge (ADR-04)
- [ ] Rollen-/Owner-Checks serverseitig — besonders bei KVG-Daten (ADR-03/05)
- [ ] Public APIs dokumentiert, keine TODOs ohne Issue-Referenz
- [ ] `.github/copilot-instructions.md` / Spezifikationen / ADR-Index aktualisiert, falls sich
      Architektur oder ubiquitäre Sprache ändert

## Test plan

<!-- Wie wurde manuell verifiziert? -->
- [ ] `./mvnw verify`
