# Shared Fragment: Definition of Done

Vor jedem Commit abprüfen.

## Tests (US- und Task-Issues)
- [ ] Unit-Tests für neue/geänderte Services und Logik vorhanden und grün
- [ ] Komponenten-Tests für neue/geänderte UI-Komponenten vorhanden und grün

## BDD (nur US-Issues)
- [ ] `.feature`-Datei unter `src/test/resources/features/` vorhanden
- [ ] Step-Definitions für alle Szenarien im richtigen Runner (Java-Cucumber oder Playwright) — **kein `@Pending`**
- [ ] Alle Szenarien grün via `mvn verify` (oder dokumentierte Begründung, warum Vitest genügt)

## Dokumentation (nur US-Issues)
- [ ] `specs/user-stories/<nr>-<slug>.md` vollständig und aktuell
- [ ] BDD-Szenarien in User-Story = BDD-Szenarien in `.feature`
- [ ] Verweis auf `.feature`-Datei in der User Story eingetragen
- [ ] Aufgaben-Checkliste in der User Story abgehakt

## Code-Qualität (US- und Task-Issues)
- [ ] Keine toten Code-Pfade, keine auskommentierten Stellen
- [ ] Keine TODOs ohne Issue-Referenz
- [ ] Passende Reviewer-Agenten liefen (siehe `/implement` → Schritt 7 „Reviewer-Runde")
